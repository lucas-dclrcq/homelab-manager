package org.hoohoot.homelab.manager.notifications.api

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.domain.mediaKey
import org.hoohoot.homelab.manager.notifications.domain.usecases.MovieDownload
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifyMovieDownloaded

@Path("/api/notifications/radarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class RadarrResource(
    private val notifyMovieDownloaded: NotifyMovieDownloaded,
) {

    @POST
    suspend fun handleRadarrNotification(payload: RadarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.debug("Ignoring radarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying movie downloaded : ${payload.title()}")

        notifyMovieDownloaded(payload.toMovieDownload())

        return Response.noContent().build()
    }

    private fun RadarrWebhookPayload.toMovieDownload(): MovieDownload {
        val rawTitle = movie?.title
        val rawYear = movie?.year
        return MovieDownload(
            title = title(),
            year = year(),
            quality = quality(),
            imdbLink = imdbId().toImdbLink(),
            requester = requester(),
            movieId = movie?.id?.toString(),
            mediaKey = if (rawTitle != null && rawYear != null) mediaKey(rawTitle, rawYear.toString()) else null,
        )
    }
}
