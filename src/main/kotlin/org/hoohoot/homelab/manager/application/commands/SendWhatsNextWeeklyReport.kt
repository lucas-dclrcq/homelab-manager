package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.Calendar
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.SonarrGateway
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder

object SendWhatsNextWeeklyReport : Command

@Startup
@ApplicationScoped
class SendWhatsNextReportWeeklyHandler(private val sonarrGateway: SonarrGateway, private val notificationGateway: NotificationGateway, private val calendar: Calendar) : CommandHandler<SendWhatsNextWeeklyReport> {
    override suspend fun handle(command: SendWhatsNextWeeklyReport) {
        Log.info("Sending whats next report")

        val currentWeek = calendar.getCurrentWeek()
        val seriesCalendar = sonarrGateway.getSeriesCalendar(currentWeek.start, currentWeek.end)

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