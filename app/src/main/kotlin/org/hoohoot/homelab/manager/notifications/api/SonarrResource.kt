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
import org.hoohoot.homelab.manager.notifications.domain.usecases.EpisodeDownload
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifyEpisodeDownloaded

@Path("/api/notifications/sonarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class SonarrResource(
    private val notifyEpisodeDownloaded: NotifyEpisodeDownloaded,
) {

    @POST
    suspend fun handleSonarrNotification(payload: SonarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.info("Ignoring sonarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying series downloaded : ${payload.seriesName()}")

        notifyEpisodeDownloaded(payload.toEpisodeDownload())

        return Response.noContent().build()
    }

    private fun SonarrWebhookPayload.toEpisodeDownload(): EpisodeDownload {
        val rawTitle = series?.title
        val rawYear = series?.year
        return EpisodeDownload(
            seriesTitle = series?.title ?: "Unknown",
            imdbLink = imdbId().toImdbLink(),
            seasonAndEpisode = seasonAndEpisodeNumber(),
            episodeTitle = episodeName(),
            quality = quality(),
            requester = requester(),
            downloadClient = downloadClient,
            indexer = indexer(),
            seriesId = series?.id?.toString(),
            mediaKey = if (rawTitle != null && rawYear != null) mediaKey(rawTitle, rawYear.toString()) else null,
        )
    }
}
