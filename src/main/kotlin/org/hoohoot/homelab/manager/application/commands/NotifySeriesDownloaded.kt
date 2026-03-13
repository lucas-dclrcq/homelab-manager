package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.Series
import org.hoohoot.homelab.manager.domain.media_notifications.toImdbLink

data class NotifySeriesDownloaded(val series: Series) : Command

@Startup
@ApplicationScoped
class NotifySeriesDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifySeriesDownloaded> {
    override suspend fun handle(command: NotifySeriesDownloaded) {
        val series = command.series

        Log.info("Notifying series downloaded : ${series.seriesName}")

        val seriesId = series.imdbId
        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${series.seriesName} [${seriesId.toImdbLink()}]")
            .addInfoLine("Episode : ${series.seasonAndEpisodeNumber} - ${series.episodeName} [${series.quality}]")
            .addInfoLine("Series requested by : ${series.requester}")
            .addInfoLine("Source : ${series.downloadClient} (${series.indexer})")
            .buildNotification()

        notificationGateway.sendMediaNotification(notification)
    }
}
