package org.hoohoot.homelab.manager.notifications.api

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifyBookDownload

@Path("/api/notifications/nana")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NanaResource(
    private val notifyBookDownload: NotifyBookDownload,
) {

    @POST
    suspend fun handleNanaNotification(payload: NanaWebhookPayload): Response {
        when (payload.event) {
            "download.succeeded" -> {
                Log.info("Notifying book downloaded : ${payload.title()}")
                notifyBookDownload(payload.toBookDownload(succeeded = true))
            }

            "download.failed" -> {
                Log.info("Notifying book download failed : ${payload.title()}")
                notifyBookDownload(payload.toBookDownload(succeeded = false))
            }

            else -> Log.debug("Ignoring nana event: ${payload.event}")
        }
        return Response.noContent().build()
    }
}
