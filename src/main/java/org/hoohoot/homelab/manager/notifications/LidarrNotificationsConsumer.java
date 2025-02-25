package org.hoohoot.homelab.manager.notifications;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage;

import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class LidarrNotificationsConsumer {
    private final MatrixAPI matrixAPI;
    private final String matrixRoomId;

    public LidarrNotificationsConsumer(@RestClient MatrixAPI matrixAPI, @ConfigProperty(name = "matrix.room_id") String matrixRoomId) {
        this.matrixAPI = matrixAPI;
        this.matrixRoomId = matrixRoomId;
    }

    @Incoming("lidarr-notifications")
    public Uni<Void> process(JsonObject payload) {
        JsonObject artist = payload.getJsonObject("artist");
        if (artist == null) return Uni.createFrom().voidItem();

        JsonObject album = payload.getJsonObject("album");
        if (album == null) return Uni.createFrom().voidItem();

        String downloadClient = payload.getString("downloadClient");
        String artistName = artist.getString("name");
        String albumTitle = album.getString("title");

        String coverUrl = album.getJsonArray("images")
                .stream()
                .map(o -> (JsonObject) o)
                .filter(image -> "cover".equals(image.getString("coverType")))
                .map(image -> image.getString("remoteUrl"))
                .findFirst().orElse("unknown");

        String genres = album.getJsonArray("genres")
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String notificationContent = "<h1>Album Downloaded</h1><p>%s - %s<br>Cover: %s<br>Genres: %s<br>Source: %s</p>".formatted(artistName, albumTitle, coverUrl, genres, downloadClient);

        return this.matrixAPI.sendMessage(this.matrixRoomId, UUID.randomUUID().toString(), MatrixMessage.html(notificationContent)).replaceWithVoid();
    }
}
