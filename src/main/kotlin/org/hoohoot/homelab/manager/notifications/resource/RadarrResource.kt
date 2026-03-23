package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.Movie
import org.hoohoot.homelab.manager.notifications.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.RadarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.arr.mediaKey
import org.hoohoot.homelab.manager.notifications.arr.toImdbLink
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@Path("/api/notifications/radarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class RadarrResource(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleRadarrNotification(payload: RadarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring radarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        val movie = Movie.from(payload)
        Log.info("Notifying movie downloaded : ${movie.title}")

        val notification = NotificationBuilder()
            .addTitle("🎬 Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("👤 Requested by : ${movie.requester}")
            .buildNotification()

        val sentId = matrixSender.sendMediaNotification(notification)

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
