package org.hoohoot.homelab.manager.notifications.kafka;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.hoohoot.homelab.manager.notifications.parser.ParseSeries;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage;
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration;

import java.util.UUID;

@ApplicationScoped
public class SonarrNotificationsConsumer {
    private final MatrixAPI matrixAPI;
    private final MatrixRoomsConfiguration matrixRooms;

    public SonarrNotificationsConsumer(@RestClient MatrixAPI matrixAPI, MatrixRoomsConfiguration matrixRooms) {
        this.matrixAPI = matrixAPI;
        this.matrixRooms = matrixRooms;
    }

    @Incoming("sonarr-notifications")
    public Uni<Void> process(JsonObject payload) {
        ParseSeries parseSeries = ParseSeries.from(payload);

        String messageContent = "<h1>Episode Downloaded</h1><p>%s - %s - %s [%s]<br>Requested by : %s<br>Source: %s (%s)</p>"
                .formatted(parseSeries.seriesName(), parseSeries.seasonAndEpisodeNumber(), parseSeries.episodeName(), parseSeries.quality(), parseSeries.requester(), parseSeries.downloadClient(), parseSeries.indexer());

        return this.matrixAPI.sendMessage(this.matrixRooms.sonarr(), UUID.randomUUID().toString(), MatrixMessage.html(messageContent)).replaceWithVoid();
    }
}
