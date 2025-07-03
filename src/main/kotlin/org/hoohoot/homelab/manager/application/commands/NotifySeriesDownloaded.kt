package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationRepository
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.ParseSeries
import org.hoohoot.homelab.manager.domain.media_notifications.toImdbLink

data class NotifySeriesDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifySeriesDownloadedHandler(private val notificationGateway: NotificationGateway, private val notificationRepository: NotificationRepository) : CommandHandler<NotifySeriesDownloaded> {
    override suspend fun handle(command: NotifySeriesDownloaded) {
        val series = ParseSeries.from(command.webhookPayload)

        Log.info("Notifying series downloaded : ${series.seriesName}")

        val seriesId = series.imdbId
        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${series.seriesName} [${seriesId.toImdbLink()}]")
            .addInfoLine("Episode : ${series.seasonAndEpisodeNumber} - ${series.episodeName} [${series.quality}]")
            .addInfoLine("Series requested by : ${series.requester}")
            .addInfoLine("Source : ${series.downloadClient} (${series.indexer})")
            .buildNotification()

        val seriesPreviousNotificationId = notificationRepository.getNotificationIdForSeries(seriesId)

        if (seriesPreviousNotificationId != null) {
            this.notificationGateway.sendMediaNotification(notification, seriesPreviousNotificationId)
        } else {
            val notificationId = this.notificationGateway.sendMediaNotification(notification)
            this.notificationRepository.saveNotificationIdForSeries(seriesId, notificationId)
        }
    }
}