package org.hoohoot.homelab.manager.notifications.kafka

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage.Companion.html
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors


class LidarrNotificationsConsumer(@param:RestClient private val matrixAPI: MatrixAPI, private val matrixRooms: MatrixRoomsConfiguration) {
    @Incoming("lidarr-notifications")
    fun process(payload: JsonObject): Uni<Void> {
        val artist = payload.getJsonObject("artist") ?: return Uni.createFrom().voidItem()

        val album = payload.getJsonObject("album") ?: return Uni.createFrom().voidItem()

        val downloadClient = payload.getString("downloadClient")
        val artistName = artist.getString("name")
        val albumTitle = album.getString("title")

        val coverUrl = album.getJsonArray("images")
            .stream()
            .map { o: Any -> o as JsonObject }
            .filter { image: JsonObject -> "cover" == image.getString("coverType") }
            .map { image: JsonObject -> image.getString("remoteUrl") }
            .findFirst().orElse("unknown")

        val genres = album.getJsonArray("genres")
            .stream()
            .map { obj: Any -> obj.toString() }
            .collect(Collectors.joining(", "))

        val releaseDate = album.getString("releaseDate")
        val year = LocalDateTime.parse(releaseDate, DateTimeFormatter.ISO_ZONED_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("yyyy"))

        val notificationContent =
            "<h1>Album Downloaded</h1><p>%s - %s (%s)<br>Cover: %s<br>Genres: %s<br>Source: %s</p>".format(
                artistName,
                albumTitle,
                year,
                coverUrl,
                genres,
                downloadClient
            )

        return matrixAPI.sendMessage(
            matrixRooms.lidarr(),
            UUID.randomUUID().toString(),
            html(notificationContent)
        ).replaceWithVoid()
    }
}
