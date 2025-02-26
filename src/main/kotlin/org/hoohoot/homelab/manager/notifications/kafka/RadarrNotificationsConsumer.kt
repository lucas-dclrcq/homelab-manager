package org.hoohoot.homelab.manager.notifications.kafka

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage.Companion.html
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration
import java.util.*

class RadarrNotificationsConsumer(
    @param:RestClient private val matrixAPI: MatrixAPI,
    private val matrixRooms: MatrixRoomsConfiguration
) {
    @Incoming("radarr-notifications")
    fun process(payload: JsonObject): Uni<Void> {
        val movie = payload.getJsonObject("movie") ?: return Uni.createFrom().voidItem()

        val title = movie.getString("title")
        val year = movie.getString("year")
        val imdbId = movie.getString("imdbId")


        val movieFile = payload.getJsonObject("movieFile")
        val quality = if (movieFile == null) "" else movieFile.getString("quality")

        val tags = movie.getJsonArray("tags")

        val notificationContent = "<h1>Movie Downloaded</h1>" +
                "<p>" +
                "%s (%s) [%s] https://www.imdb.com/title/%s/<br>".format(title, year, quality, imdbId) +
                "Requested by : %s".format(requester(tags)) +
                "</p>"

        return matrixAPI.sendMessage(
            matrixRooms.radarr(),
            UUID.randomUUID().toString(),
            html(notificationContent)
        ).replaceWithVoid()
    }

    companion object {
        @Deprecated("")
        fun requester(tags: JsonArray?): String {
            if (tags == null) return "unknown"

            return tags.stream()
                .map { obj: Any -> obj.toString() }
                .filter { tag: String -> tag.matches("\\d+ - \\w+".toRegex()) }
                .map { tag: String -> tag.split(" - ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] }
                .findFirst()
                .orElse("unknown")
        }
    }
}
