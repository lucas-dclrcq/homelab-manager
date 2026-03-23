package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.Album
import org.hoohoot.homelab.manager.notifications.LidarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.matrix.MatrixConfiguration
import org.hoohoot.homelab.manager.notifications.matrix.sendNotification

@Path("/api/notifications/lidarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class LidarrResource(
    private val matrixClient: MatrixClientServerApiClient,
    private val matrixConfig: MatrixConfiguration,
) {

    @POST
    suspend fun handleLidarrNotification(payload: LidarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        val album = Album.from(payload)
        Log.info("Notifying album downloaded : ${album.albumTitle}")

        val content = RoomMessageEventContent.TextBased.Text(
            body = """
                🎵 Album downloaded
                ${album.artistName} - ${album.albumTitle} (${album.year})
                🖼️ Cover: ${album.coverUrl}
                🎸 Genres : ${album.genres.joinToString(", ")}
                📥 Source : ${album.downloadClient}
            """.trimIndent(),
            format = "org.matrix.custom.html",
            formattedBody = "<h1>🎵 Album downloaded</h1>" +
                "<p>${album.artistName} - ${album.albumTitle} (${album.year})" +
                "<br>🖼️ Cover: ${album.coverUrl}" +
                "<br>🎸 Genres : ${album.genres.joinToString(", ")}" +
                "<br>📥 Source : ${album.downloadClient}</p>"
        )

        matrixClient.sendNotification(content, matrixConfig.room().music())
        return Response.noContent().build()
    }
}
