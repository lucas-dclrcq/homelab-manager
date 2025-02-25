package org.hoohoot.homelab.manager.notifications;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class SonarrNotificationsConsumer {
    private final MatrixAPI matrixAPI;
    private final String matrixRoomId;

    public SonarrNotificationsConsumer(@RestClient MatrixAPI matrixAPI, @ConfigProperty(name = "matrix.room_id") String matrixRoomId) {
        this.matrixAPI = matrixAPI;
        this.matrixRoomId = matrixRoomId;
    }

    @Incoming("sonarr-notifications")
    public Uni<Void> process(JsonObject payload) {
        JsonObject series = payload.getJsonObject("series");
        if (series == null) return Uni.createFrom().voidItem();

        String seriesName = series.getString("title");

        Optional<JsonObject> episode = payload.getJsonArray("episodes").stream().map(o -> (JsonObject) o).findFirst();
        String episodeNumber = episode.map(e -> e.getString("episodeNumber")).orElse("");
        String seasonNumber = episode.map(e -> e.getString("seasonNumber")).orElse("");
        String episodeName = episode.map(e -> e.getString("title")).orElse("");

        Optional<JsonObject> episodeFile = payload.getJsonArray("episodeFiles").stream().map(o -> (JsonObject) o).findFirst();
        String quality = episodeFile.map(e -> e.getString("quality")).orElse("");

        String requester = ParseRequester.fromTags(series.getJsonArray("tags"));
        String downloadClient = payload.getString("downloadClient");

        JsonObject release = payload.getJsonObject("release");
        String indexer = release == null ? "" : release.getString("indexer").replace(" (Prowlarr)", "");

        String notificationContent = "<h1>Episode Downloaded</h1><p>%s - %sx%s - %s [%s]<br>Requested by : %s<br>Source: %s (%s)</p>"
                .formatted(seriesName, seasonNumber, episodeNumber, episodeName, quality, requester, downloadClient, indexer);

        return this.matrixAPI.sendMessage(this.matrixRoomId, UUID.randomUUID().toString(), MatrixMessage.html(notificationContent)).replaceWithVoid();
    }
}
