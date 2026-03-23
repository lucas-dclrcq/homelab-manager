package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.arr.mediaKey
import org.hoohoot.homelab.manager.notifications.arr.requester
import org.hoohoot.homelab.manager.notifications.arr.sonarr.SonarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.arr.toImdbLink
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

private const val DEFAULT_VALUE = "unknown"

@Path("/api/notifications/sonarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class SonarrResource(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleSonarrNotification(payload: SonarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.info("Ignoring sonarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying series downloaded : ${payload.seriesName()}")

        val notification = NotificationBuilder()
            .addTitle("📺 Episode Downloaded")
            .addInfoLine("📡 Series : ${payload.series?.title ?: "Unknown"} [${payload.imdbId().toImdbLink()}]")
            .addInfoLine("🎞️ Episode : ${payload.seasonAndEpisodeNumber()} - ${payload.episodeName()} [${payload.quality()}]")
            .addInfoLine("👤 Series requested by : ${payload.requester()}")
            .addInfoLine("📥 Source : ${payload.downloadClient} (${payload.indexer()})")
            .buildNotification()

        val seriesId = payload.series?.id?.toString()

        if (seriesId != null) {
            val activeThread = notificationRepo.getThreadByMediaId(seriesId, "series")
            val sentNotificationId = matrixSender.sendMediaNotification(notification, activeThread)
            val threadEventId = activeThread ?: sentNotificationId

            val title = payload.series.title
            val year = payload.series.year
            val key = if (title != null && year != null) mediaKey(title, year.toString()) else null
            notificationRepo.saveOrUpdateThread(seriesId, "series", key, threadEventId)
        } else {
            matrixSender.sendMediaNotification(notification)
        }

        return Response.noContent().build()
    }
}

private fun SonarrWebhookPayload.quality(): String = this.episodeFile
    ?.quality
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.seriesName(): String = this.series
    ?.title
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.seasonAndEpisodeNumber(): String = this.episodes
    ?.firstOrNull()
    ?.let { "S%02dE%02d".format(it.seasonNumber, it.episodeNumber) }
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.episodeName(): String = this.episodes
    ?.firstOrNull()
    ?.title
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.indexer(): String = this.release
    ?.indexer
    ?.replace(" (Prowlarr)", "")
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.imdbId(): String = this.series
    ?.imdbId
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.requester(): String = this.series?.tags
    ?.requester()
    ?: DEFAULT_VALUE
