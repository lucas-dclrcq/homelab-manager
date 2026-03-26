package org.hoohoot.homelab.devservices.synapse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SynapseContainer {

    private static final String SHARED_SECRET = "test-shared-secret";
    private static final int SYNAPSE_PORT = 8008;
    private static final String BOT_USERNAME = "johnnybot";
    private static final String BOT_PASSWORD = "botpassword";

    private static final String LOG_CONFIG = """
            version: 1
            formatters:
              precise:
                format: '%(asctime)s - %(name)s - %(lineno)d - %(levelname)s - %(request)s - %(message)s'
            handlers:
              console:
                class: logging.StreamHandler
                formatter: precise
            root:
              level: WARNING
              handlers: [console]
            """;

    private static final String STARTUP_SCRIPT = """
            #!/bin/sh
            mkdir -p /data
            cp /conf/homeserver.yaml /data/homeserver.yaml
            cp /conf/localhost.log.config /data/localhost.log.config
            python3 -c "from signedjson.key import generate_signing_key, write_signing_keys; from sys import stdout; write_signing_keys(stdout, [generate_signing_key('auto')])" > /data/localhost.signing.key
            exec python3 -m synapse.app.homeserver --config-path /data/homeserver.yaml
            """;

    private GenericContainer<?> container;
    private String synapseUrl;
    private String adminAccessToken;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record StartResult(
            String synapseUrl,
            String adminAccessToken,
            String botAccessToken,
            String mediaRoomId,
            String musicRoomId,
            String supportRoomId
    ) {}

    public StartResult start(String synapseImage) {
        String homeserverYaml = loadResource("synapse/homeserver.yaml");

        container = new GenericContainer<>(synapseImage)
                .withExposedPorts(SYNAPSE_PORT)
                .withCopyToContainer(Transferable.of(homeserverYaml), "/conf/homeserver.yaml")
                .withCopyToContainer(Transferable.of(LOG_CONFIG), "/conf/localhost.log.config")
                .withCopyToContainer(Transferable.of(STARTUP_SCRIPT, 0x1ed /* 755 */), "/conf/start.sh")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("/conf/start.sh"))
                .withCommand()
                .waitingFor(Wait.forHttp("/_matrix/client/versions").forPort(SYNAPSE_PORT).forStatusCode(200));

        container.start();

        synapseUrl = "http://" + container.getHost() + ":" + container.getMappedPort(SYNAPSE_PORT);

        adminAccessToken = registerUser("admin", "admin");
        disableRateLimit(adminAccessToken, "@admin:localhost");

        String botAccessToken = registerUser(BOT_USERNAME, BOT_PASSWORD);
        disableRateLimit(adminAccessToken, "@" + BOT_USERNAME + ":localhost");

        String mediaRoomId = createRoom("media");
        String musicRoomId = createRoom("music");
        String supportRoomId = createRoom("support");

        inviteAndJoin(mediaRoomId, "@" + BOT_USERNAME + ":localhost", botAccessToken);
        inviteAndJoin(musicRoomId, "@" + BOT_USERNAME + ":localhost", botAccessToken);
        inviteAndJoin(supportRoomId, "@" + BOT_USERNAME + ":localhost", botAccessToken);

        return new StartResult(synapseUrl, adminAccessToken, botAccessToken, mediaRoomId, musicRoomId, supportRoomId);
    }

    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

    private String registerUser(String username, String password) {
        try {
            HttpResponse<String> nonceResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_synapse/admin/v1/register"))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            String nonce = objectMapper.readTree(nonceResponse.body()).get("nonce").asText();

            boolean isAdmin = "admin".equals(username);
            String mac = computeHmac(nonce, username, password, isAdmin);

            Map<String, Object> registerBody = new HashMap<>();
            registerBody.put("nonce", nonce);
            registerBody.put("username", username);
            registerBody.put("password", password);
            registerBody.put("admin", isAdmin);
            registerBody.put("mac", mac);

            HttpResponse<String> registerResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_synapse/admin/v1/register"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(registerBody)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (registerResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to register user '" + username + "': " + registerResponse.body());
            }

            return objectMapper.readTree(registerResponse.body()).get("access_token").asText();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to register user '" + username + "'", e);
        }
    }

    private void disableRateLimit(String accessToken, String userId) {
        try {
            Map<String, Object> body = Map.of("messages_per_second", 0, "burst_count", 0);

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_synapse/admin/v1/users/" + userId + "/override_ratelimit"))
                            .header("Authorization", "Bearer " + accessToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to disable rate limit for " + userId + ": " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to disable rate limit for " + userId, e);
        }
    }

    private String createRoom(String name) {
        try {
            Map<String, Object> body = Map.of("name", name, "visibility", "private");

            HttpResponse<String> response = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_matrix/client/r0/createRoom"))
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to create room '" + name + "': " + response.body());
            }

            return objectMapper.readTree(response.body()).get("room_id").asText();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create room '" + name + "'", e);
        }
    }

    private void inviteAndJoin(String roomId, String userId, String userAccessToken) {
        try {
            // Invite
            Map<String, Object> inviteBody = Map.of("user_id", userId);
            HttpResponse<String> inviteResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_matrix/client/r0/rooms/" + roomId + "/invite"))
                            .header("Authorization", "Bearer " + adminAccessToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(inviteBody)))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (inviteResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to invite " + userId + " to room " + roomId + ": " + inviteResponse.body());
            }

            // Join
            HttpResponse<String> joinResponse = httpClient.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(synapseUrl + "/_matrix/client/r0/rooms/" + roomId + "/join"))
                            .header("Authorization", "Bearer " + userAccessToken)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString("{}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (joinResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to join room " + roomId + " as " + userId + ": " + joinResponse.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to invite/join " + userId + " in room " + roomId, e);
        }
    }

    private String computeHmac(String nonce, String username, String password, boolean admin) {
        try {
            SecretKeySpec key = new SecretKeySpec(SHARED_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            String message = nonce + "\0" + username + "\0" + password + "\0" + (admin ? "admin" : "notadmin");
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : rawHmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    private String loadResource(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + path, e);
        }
    }
}
