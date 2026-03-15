package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.requester
import org.hoohoot.homelab.manager.domain.media_notifications.toImdbLink
import org.hoohoot.homelab.manager.infrastructure.api.dto.SonarrWebhookPayload
import org.hoohoot.homelab.manager.infrastructure.api.dto.SonarrWebhookSeries

private const val DEFAULT_VALUE = "unknown"

data class NotifySeriesDownloaded(val notification: SonarrWebhookPayload) : Command

@Startup
@ApplicationScoped
class NotifySeriesDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifySeriesDownloaded> {
    override suspend fun handle(command: NotifySeriesDownloaded) {
        if (command.notification.eventType != "Download") {
            Log.info("Ignoring sonarr event: ${command.notification.eventType}")
            return
        }

        Log.info("Notifying series downloaded : ${command.notification.seriesName()}")

        val seriesId = command.notification.imdbId()
        
        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${command.notification.series?.title ?: "Unknown"} [${seriesId?.toImdbLink()}]")
            .addInfoLine("Episode : ${command.notification.seasonAndEpisodeNumber()} - ${command.notification.episodeName()} [${command.notification.quality()}]")
            .addInfoLine("Series requested by : ${command.notification.requester()}")
            .addInfoLine("Source : ${command.notification.downloadClient} (${command.notification.indexer()})")
            .buildNotification()

        notificationGateway.sendMediaNotification(notification)
    }

    private fun SonarrWebhookPayload.quality(): String = this.episodeFile
        ?.quality
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.seriesName(): String = this.series
        ?.title
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.seasonAndEpisodeNumber(): String = this.episodes
        ?.firstOrNull()
        ?.let {
            val episodeNumber = it.episodeNumber
            val seasonNumber = it.seasonNumber
            "S%02dE%02d".format(seasonNumber, episodeNumber)
        }
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.episodeName(): String = this.episodes
        ?.firstOrNull()
        ?.title
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.downloadClient(): String = this.downloadClient ?: DEFAULT_VALUE

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
}
