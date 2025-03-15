package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.NotificationBuilder
import org.hoohoot.homelab.manager.domain.ParseSeries
import org.hoohoot.homelab.manager.domain.toImdbLink

data class NotifySeriesDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifySeriesDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifySeriesDownloaded> {
    override suspend fun handle(command: NotifySeriesDownloaded) {
        val series = ParseSeries.from(command.webhookPayload)

        Log.info("Notifying series downloaded : ${series.seriesName}")

        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${series.seriesName} [${series.imdbId.toImdbLink()}]")
            .addInfoLine("Episode : ${series.seasonAndEpisodeNumber} - ${series.episodeName} [${series.quality}]")
            .addInfoLine("Series requested by : ${series.requester}")
            .addInfoLine("Source : ${series.downloadClient} (${series.indexer})")
            .buildNotification()

        this.notificationGateway.sendMediaNotification(notification)
    }
}