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

@ApplicationScoped
public class RadarrNotificationsConsumer {
    private final MatrixAPI matrixAPI;
    private final String matrixRoomId;

    public RadarrNotificationsConsumer(@RestClient MatrixAPI matrixAPI, @ConfigProperty(name = "matrix.room_id") String matrixRoomId) {
        this.matrixAPI = matrixAPI;
        this.matrixRoomId = matrixRoomId;
    }

    @Incoming("radarr-notifications")
    public Uni<Void> process(JsonObject payload) {
        JsonObject movie = payload.getJsonObject("movie");

        if (movie == null) return Uni.createFrom().voidItem();

        String title = movie.getString("title");
        String year = movie.getString("year");
        String imdbId = movie.getString("imdbId");


        JsonObject movieFile = payload.getJsonObject("movieFile");
        String quality = movieFile == null ? "" : movieFile.getString("quality");

        JsonArray tags = movie.getJsonArray("tags");

        String notificationContent = "<h1>Movie Downloaded</h1>" +
                                     "<p>" +
                                     "%s (%s) [%s] https://www.imdb.com/title/%s/<br>".formatted(title, year, quality, imdbId) +
                                     "Requested by : %s".formatted(userTag(tags)) +
                                     "</p>";

        return this.matrixAPI.sendMessage(this.matrixRoomId, UUID.randomUUID().toString(), MatrixMessage.html(notificationContent)).replaceWithVoid();
    }

    public String userTag(JsonArray tags) {
        if (tags == null) return "unknown";

        return tags.stream()
                .map(Object::toString)
                .filter(tag -> tag.matches("\\d+ - \\w+"))
                .map(tag -> tag.split(" - ")[1])
                .findFirst()
                .orElse("unknown");
    }
}
