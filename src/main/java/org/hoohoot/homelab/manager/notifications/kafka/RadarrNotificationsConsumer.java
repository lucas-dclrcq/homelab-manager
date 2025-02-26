package org.hoohoot.homelab.manager.notifications.kafka;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration;

import java.util.UUID;

@ApplicationScoped
public class RadarrNotificationsConsumer {
    private final MatrixAPI matrixAPI;
    private final MatrixRoomsConfiguration matrixRooms;

    public RadarrNotificationsConsumer(@RestClient MatrixAPI matrixAPI, MatrixRoomsConfiguration matrixRooms) {
        this.matrixAPI = matrixAPI;
        this.matrixRooms = matrixRooms;
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
                                     "Requested by : %s".formatted(requester(tags)) +
                                     "</p>";

        return this.matrixAPI.sendMessage(this.matrixRooms.radarr(), UUID.randomUUID().toString(), MatrixMessage.html(notificationContent)).replaceWithVoid();
    }

    @Deprecated
    public static String requester(JsonArray tags) {
        if (tags == null) return "unknown";

        return tags.stream()
                .map(Object::toString)
                .filter(tag -> tag.matches("\\d+ - \\w+"))
                .map(tag -> tag.split(" - ")[1])
                .findFirst()
                .orElse("unknown");
    }
}
