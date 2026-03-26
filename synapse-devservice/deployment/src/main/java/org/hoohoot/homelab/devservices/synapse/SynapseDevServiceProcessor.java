package org.hoohoot.homelab.devservices.synapse;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SynapseDevServiceProcessor {

    private static final Logger log = Logger.getLogger(SynapseDevServiceProcessor.class);

    private static final String DEFAULT_SYNAPSE_IMAGE = "matrixdotorg/synapse:v1.150.0";
    private static final String DEFAULT_ELEMENT_IMAGE = "vectorim/element-web:latest";

    private static volatile SynapseContainer synapseContainer;
    private static volatile GenericContainer<?> elementContainer;
    private static volatile Map<String, String> devServiceProperties;

    @BuildStep(onlyIfNot = IsNormal.class)
    public DevServicesResultBuildItem startSynapse() {
        if (devServiceProperties != null) {
            return new DevServicesResultBuildItem("synapse", null, devServiceProperties);
        }

        log.info("Starting Synapse DevService...");

        synapseContainer = new SynapseContainer();
        SynapseContainer.StartResult result = synapseContainer.start(DEFAULT_SYNAPSE_IMAGE);

        // Start Element Web
        int synapseHostPort = extractPort(result.synapseUrl());
        String elementConfigJson = buildElementConfig(synapseHostPort);

        elementContainer = new GenericContainer<>(DEFAULT_ELEMENT_IMAGE)
                .withExposedPorts(80)
                .withCopyToContainer(
                        Transferable.of(elementConfigJson.getBytes(StandardCharsets.UTF_8)),
                        "/app/config.json"
                );
        elementContainer.start();

        int elementPort = elementContainer.getMappedPort(80);
        String elementUrl = "http://localhost:" + elementPort;

        Path botDataDirectory;
        try {
            botDataDirectory = Files.createTempDirectory("matrix-bot-dev");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bot data directory", e);
        }

        Map<String, String> props = new HashMap<>();
        props.put("matrix.base-url", result.synapseUrl());
        props.put("matrix.access-token", result.adminAccessToken());
        props.put("matrix.bot.enabled", "true");
        props.put("matrix.bot.base-url", result.synapseUrl());
        props.put("matrix.bot.username", "johnnybot");
        props.put("matrix.bot.password", "botpassword");
        props.put("matrix.bot.data-directory", botDataDirectory.toString());
        props.put("matrix.bot.prefix", "johnny");
        props.put("matrix.bot.users", "localhost");
        props.put("matrix.bot.admins", "@admin:localhost");
        props.put("matrix.room.media", result.mediaRoomId());
        props.put("matrix.room.music", result.musicRoomId());
        props.put("matrix.room.support", result.supportRoomId());

        devServiceProperties = props;

        log.infof("Synapse DevService started: %s", result.synapseUrl());
        log.infof("Element Web available at: %s", elementUrl);
        log.infof("Rooms created - media: %s, music: %s, support: %s",
                result.mediaRoomId(), result.musicRoomId(), result.supportRoomId());

        return new DevServicesResultBuildItem("synapse", null, props);
    }

    @BuildStep(onlyIfNot = IsNormal.class)
    public CardPageBuildItem devUICard() {
        CardPageBuildItem card = new CardPageBuildItem();
        card.addPage(Page.webComponentPageBuilder()
                .title("Synapse")
                .icon("font-awesome-solid:message")
                .componentLink("qwc-synapse-card.js"));
        return card;
    }

    private int extractPort(String url) {
        String portStr = url.substring(url.lastIndexOf(':') + 1);
        return Integer.parseInt(portStr);
    }

    private String buildElementConfig(int synapseHostPort) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> homeserver = new HashMap<>();
            homeserver.put("base_url", "http://localhost:" + synapseHostPort);
            homeserver.put("server_name", "localhost");

            Map<String, Object> serverConfig = new HashMap<>();
            serverConfig.put("m.homeserver", homeserver);

            Map<String, Object> config = new HashMap<>();
            config.put("default_server_config", serverConfig);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Element config", e);
        }
    }
}
