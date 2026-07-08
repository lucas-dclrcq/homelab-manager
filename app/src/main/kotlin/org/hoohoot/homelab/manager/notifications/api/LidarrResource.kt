package org.hoohoot.homelab.manager.notifications.api

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.domain.usecases.AlbumDownload
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifyAlbumDownloaded

@Path("/api/notifications/lidarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class LidarrResource(
    private val notifyAlbumDownloaded: NotifyAlbumDownloaded,
) {

    @POST
    suspend fun handleLidarrNotification(payload: LidarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying album downloaded : ${payload.albumTitle()}")

        notifyAlbumDownloaded(
            AlbumDownload(
                artistName = payload.artistName(),
                albumTitle = payload.albumTitle(),
                year = payload.year(),
                coverUrl = payload.coverUrl(),
                genres = payload.genres(),
                source = payload.source(),
            ),
        )
        return Response.noContent().build()
    }
}
