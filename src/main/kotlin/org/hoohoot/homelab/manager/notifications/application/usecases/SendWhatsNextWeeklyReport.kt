package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.Calendar
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.application.ports.arr.ArrGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import java.time.Clock
import java.time.LocalDateTime

object SendWhatsNextWeeklyReport : Command

@Startup
@ApplicationScoped
class SendWhatsNextReportWeeklyHandler(private val arrGateway: ArrGateway, private val notificationGateway: NotificationGateway, private val calendar: Calendar) : CommandHandler<SendWhatsNextWeeklyReport> {
    override suspend fun handle(command: SendWhatsNextWeeklyReport) {
        val currentWeek = calendar.getCurrentWeek()
        val seriesCalendar = arrGateway.getSeriesCalendar(currentWeek.start, currentWeek.end)

        val scheduledSeries = seriesCalendar
            .map { "${it.airDate} : ${it.series?.title} - ${"S%02dE%02d".format(it.seasonNumber, it.episodeNumber)} - ${it.title}" }

        val notification = NotificationBuilder()
            .addTitle("What's next report")
            .addEmptyLine()
            .addInfoLine("Series :")
            .addInfoLines(scheduledSeries)
            .buildNotification()

        this.notificationGateway.sendMediaNotification(notification)
    }

}