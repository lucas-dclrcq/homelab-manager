package org.hoohoot.homelab.manager.notifications.kafka

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage.Companion.html
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration
import org.hoohoot.homelab.manager.notifications.parser.ParseSeries.Companion.from
import java.util.*

@ApplicationScoped
class SonarrNotificationsConsumer(
    @param:RestClient private val matrixAPI: MatrixAPI,
    private val matrixRooms: MatrixRoomsConfiguration
) {
    @Incoming("sonarr-notifications")
    fun process(payload: JsonObject): Uni<Void> {
        val parseSeries = from(payload)

        val messageContent =
            "<h1>Episode Downloaded</h1><p>Series : %s [%s]<br>Episode : %s - %s [%s]<br>Series requested by : %s<br>Source: %s (%s)</p>"
                .format(
                    parseSeries.seriesName(),
                    parseSeries.imdbLink(),
                    parseSeries.seasonAndEpisodeNumber(),
                    parseSeries.episodeName(),
                    parseSeries.quality(),
                    parseSeries.requester(),
                    parseSeries.downloadClient(),
                    parseSeries.indexer()
                )

        return matrixAPI.sendMessage(
            matrixRooms.sonarr(),
            UUID.randomUUID().toString(),
            html(messageContent)
        ).replaceWithVoid()
    }
}
