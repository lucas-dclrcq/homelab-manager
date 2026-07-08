package org.hoohoot.homelab.manager.notifications.api

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifySubtitleDownloaded

@Path("/api/notifications/bazarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class BazarrResource(
    private val notifySubtitleDownloaded: NotifySubtitleDownloaded,
) {

    @POST
    suspend fun handleBazarrNotification(payload: BazarrWebhookPayload): Response {
        val subtitle = payload.toSubtitleDownload()
        Log.info("Notifying subtitle downloaded for: ${subtitle.mediaTitle}")

        notifySubtitleDownloaded(subtitle)

        return Response.noContent().build()
    }
}
