package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.JellystatGateway
import org.hoohoot.homelab.manager.notifications.application.ports.JellystatMediaType
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder

object SendMediaStatisticsMonthlyReport : Command

@Startup
@ApplicationScoped
class SendMediaStatisticsMonthlyReportHandler(private val jellystatGateway: JellystatGateway, private val notificationGateway: NotificationGateway) : CommandHandler<SendMediaStatisticsMonthlyReport> {
    override suspend fun handle(command: SendMediaStatisticsMonthlyReport) {
        val mostPopularSeries = this.jellystatGateway.getMostPopularByType(30, JellystatMediaType.Series)

        val mostPopularSeriesFormatted = mostPopularSeries
                .sortedByDescending { it.uniqueViewers }
                .mapIndexed { index, series -> "${index+1}. ${series.name} : ${series.uniqueViewers} viewers" }

        val notification = NotificationBuilder()
            .addTitle("Top of the Month")
            .addEmptyLine()
            .addBoldInfoLine("Most popular series")
            .addInfoLines(mostPopularSeriesFormatted)
            .buildNotification()

        this.notificationGateway.sendMediaNotification(notification)
    }

}