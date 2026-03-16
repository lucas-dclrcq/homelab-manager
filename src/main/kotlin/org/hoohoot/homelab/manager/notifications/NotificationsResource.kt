package org.hoohoot.homelab.manager.notifications

import io.quarkus.logging.Log
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationsResource(
    private val notificationService: NotificationService
) {

    @POST
    @Path("/radarr")
    suspend fun handleRadarrNotification(payload: RadarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring radarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        notificationService.notifyMovieDownloaded(payload)
        return Response.noContent().build()
    }

    @POST
    @Path("/sonarr")
    suspend fun handleSonarrNotification(payload: SonarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.info("Ignoring sonarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        notificationService.notifyEpisodeDownloaded(payload)
        return Response.noContent().build()
    }

    @POST
    @Path("/lidarr")
    suspend fun handleLidarrNotification(payload: LidarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        notificationService.notifyAlbumDownloaded(payload)
        return Response.noContent().build()
    }

    @POST
    @Path("/jellyseerr")
    suspend fun handleJellyseerrNotification(payload: JellyseerrWebhookPayload): Response {
        val issue = Issue.from(payload)
        notificationService.handleJellyseerrEvent(issue)
        return Response.noContent().build()
    }

    @POST
    @Path("/bazarr")
    suspend fun handleBazarrNotification(payload: BazarrWebhookPayload): Response {
        notificationService.notifySubtitleDownloaded(payload)
        return Response.noContent().build()
    }

    @POST
    @Path("/send-whats-next-report")
    suspend fun sendWhatsNextReport() {
        notificationService.sendWhatsNextReport()
    }
}
