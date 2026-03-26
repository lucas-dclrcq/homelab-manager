package org.hoohoot.homelab.manager.it.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector.AnnotatedAndMatchesType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.Transferable
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class SynapseTestResource : QuarkusTestResourceLifecycleManager {

    private var container: GenericContainer<*>? = null
    private var synapseClient: SynapseClient? = null
    private var botEnabled: Boolean = false
    private var botDataDirectory: Path? = null

    companion object {
        private const val SHARED_SECRET = "test-shared-secret"
        private const val SYNAPSE_PORT = 8008
        private const val BOT_USERNAME = "johnnybot"
        private const val BOT_PASSWORD = "botpassword"

        private val LOG_CONFIG = """
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
        """.trimIndent()
    }

    override fun start(): Map<String, String> {
        val homeserverYaml = javaClass.classLoader.getResource("synapse/homeserver.yaml")!!.readText()

        val startupScript = """
            #!/bin/sh
            mkdir -p /data
            cp /conf/homeserver.yaml /data/homeserver.yaml
            cp /conf/localhost.log.config /data/localhost.log.config
            python3 -c "from signedjson.key import generate_signing_key, write_signing_keys; from sys import stdout; write_signing_keys(stdout, [generate_signing_key('auto')])" > /data/localhost.signing.key
            exec python3 -m synapse.app.homeserver --config-path /data/homeserver.yaml
        """.trimIndent()

        container = GenericContainer("matrixdotorg/synapse:v1.150.0")
            .withExposedPorts(SYNAPSE_PORT)
            .withCopyToContainer(Transferable.of(homeserverYaml), "/conf/homeserver.yaml")
            .withCopyToContainer(Transferable.of(LOG_CONFIG), "/conf/localhost.log.config")
            .withCopyToContainer(Transferable.of(startupScript, 0x1ed /* 755 */), "/conf/start.sh")
            .withCreateContainerCmdModifier { cmd ->
                cmd.withEntrypoint("/conf/start.sh")
            }
            .withCommand()
            .waitingFor(Wait.forHttp("/_matrix/client/versions").forPort(SYNAPSE_PORT).forStatusCode(200))

        container!!.start()

        val synapseUrl = "http://${container!!.host}:${container!!.getMappedPort(SYNAPSE_PORT)}"
        val httpClient = HttpClient.newHttpClient()
        val objectMapper = ObjectMapper()

        val accessToken = registerUser(synapseUrl, httpClient, objectMapper, "admin", "admin")
        disableRateLimit(synapseUrl, httpClient, objectMapper, accessToken, "@admin:localhost")

        synapseClient = SynapseClient(synapseUrl, accessToken, httpClient, objectMapper)

        registerUser(synapseUrl, httpClient, objectMapper, BOT_USERNAME, BOT_PASSWORD)
        disableRateLimit(synapseUrl, httpClient, objectMapper, accessToken, "@$BOT_USERNAME:localhost")
        botDataDirectory = Files.createTempDirectory("matrix-bot-test")

        return mapOf(
            "matrix.base-url" to synapseUrl,
            "matrix.access-token" to accessToken,
            "matrix.room.media" to "!placeholder:localhost",
            "matrix.room.music" to "!placeholder:localhost",
            "matrix.room.support" to "!placeholder:localhost",
            "matrix.bot.enabled" to "true",
            "matrix.bot.base-url" to synapseUrl,
            "matrix.bot.username" to BOT_USERNAME,
            "matrix.bot.password" to BOT_PASSWORD,
            "matrix.bot.data-directory" to botDataDirectory!!.toString(),
            "matrix.bot.prefix" to "bot",
            "matrix.bot.users" to "localhost",
            "matrix.bot.admins" to "@admin:localhost"
        )
    }

    override fun stop() {
        container?.stop()
        botDataDirectory?.toFile()?.deleteRecursively()
    }

    override fun inject(testInjector: TestInjector) {
        testInjector.injectIntoFields(
            synapseClient,
            AnnotatedAndMatchesType(InjectSynapse::class.java, SynapseClient::class.java)
        )
    }

    private fun registerUser(
        synapseUrl: String,
        httpClient: HttpClient,
        objectMapper: ObjectMapper,
        username: String,
        password: String
    ): String {
        val nonceResponse = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_synapse/admin/v1/register"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        val nonce = objectMapper.readTree(nonceResponse.body()).get("nonce").asText()

        val isAdmin = username == "admin"
        val mac = computeHmac(nonce, username, password, admin = isAdmin)

        val registerBody = objectMapper.writeValueAsString(
            mapOf(
                "nonce" to nonce,
                "username" to username,
                "password" to password,
                "admin" to isAdmin,
                "mac" to mac
            )
        )

        val registerResponse = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_synapse/admin/v1/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (registerResponse.statusCode() != 200) {
            throw RuntimeException("Failed to register user '$username': ${registerResponse.body()}")
        }

        return objectMapper.readTree(registerResponse.body()).get("access_token").asText()
    }

    private fun disableRateLimit(
        synapseUrl: String,
        httpClient: HttpClient,
        objectMapper: ObjectMapper,
        adminAccessToken: String,
        userId: String
    ) {
        val body = objectMapper.writeValueAsString(
            mapOf("messages_per_second" to 0, "burst_count" to 0)
        )

        val response = httpClient.send(
            HttpRequest.newBuilder()
                .uri(URI.create("$synapseUrl/_synapse/admin/v1/users/$userId/override_ratelimit"))
                .header("Authorization", "Bearer $adminAccessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        if (response.statusCode() != 200) {
            throw RuntimeException("Failed to disable rate limit for $userId: ${response.body()}")
        }
    }

    private fun computeHmac(nonce: String, username: String, password: String, admin: Boolean): String {
        val key = SecretKeySpec(SHARED_SECRET.toByteArray(), "HmacSHA1")
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(key)
        val message = "$nonce\u0000$username\u0000$password\u0000${if (admin) "admin" else "notadmin"}"
        val rawHmac = mac.doFinal(message.toByteArray())
        return rawHmac.joinToString("") { "%02x".format(it) }
    }
}
