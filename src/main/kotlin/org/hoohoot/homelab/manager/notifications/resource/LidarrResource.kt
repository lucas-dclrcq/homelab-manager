package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.Album
import org.hoohoot.homelab.manager.notifications.LidarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender

@Path("/api/notifications/lidarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class LidarrResource(
    private val matrixSender: MatrixNotificationSender,
) {

    @POST
    suspend fun handleLidarrNotification(payload: LidarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        val album = Album.from(payload)
        Log.info("Notifying album downloaded : ${album.albumTitle}")

        val notification = NotificationBuilder()
            .addTitle("🎵 Album downloaded")
            .addInfoLine("${album.artistName} - ${album.albumTitle} (${album.year})")
            .addInfoLine("🖼️ Cover: ${album.coverUrl}")
            .addInfoLine("🎸 Genres : ${album.genres.joinToString(", ")}")
            .addInfoLine("📥 Source : ${album.downloadClient}")
            .buildNotification()

        matrixSender.sendMusicNotification(notification)
        return Response.noContent().build()
    }
}
