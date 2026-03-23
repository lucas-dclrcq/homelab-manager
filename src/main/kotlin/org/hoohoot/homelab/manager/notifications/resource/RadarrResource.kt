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
import org.hoohoot.homelab.manager.notifications.RadarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.arr.mediaKey
import org.hoohoot.homelab.manager.notifications.arr.toImdbLink
import org.hoohoot.homelab.manager.notifications.imdbId
import org.hoohoot.homelab.manager.notifications.quality
import org.hoohoot.homelab.manager.notifications.requester
import org.hoohoot.homelab.manager.notifications.title
import org.hoohoot.homelab.manager.notifications.year
import org.hoohoot.homelab.manager.notifications.matrix.MatrixConfiguration
import org.hoohoot.homelab.manager.notifications.matrix.sendNotification
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@Path("/api/notifications/radarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class RadarrResource(
    private val matrixClient: MatrixClientServerApiClient,
    private val matrixConfig: MatrixConfiguration,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleRadarrNotification(payload: RadarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring radarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying movie downloaded : ${payload.title()}")

        val content = RoomMessageEventContent.TextBased.Text(
            body = "🎬 Movie Downloaded\n${payload.title()} (${payload.year()}) [${payload.quality()}] ${payload.imdbId().toImdbLink()}\n👤 Requested by : ${payload.requester()}",
            format = "org.matrix.custom.html",
            formattedBody = "<h1>🎬 Movie Downloaded</h1><p>${payload.title()} (${payload.year()}) [${payload.quality()}] ${payload.imdbId().toImdbLink()}<br>👤 Requested by : ${payload.requester()}</p>"
        )

        val sentId = matrixClient.sendNotification(content, matrixConfig.room().media())

        val movieId = payload.movie?.id?.toString()
        val title = payload.movie?.title
        val year = payload.movie?.year
        if (movieId != null) {
            val key = if (title != null && year != null) mediaKey(title, year.toString()) else null
            notificationRepo.saveOrUpdateThread(movieId, "movie", key, sentId)
        }

        return Response.noContent().build()
    }
}
