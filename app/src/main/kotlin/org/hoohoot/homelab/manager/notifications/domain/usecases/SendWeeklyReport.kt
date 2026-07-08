package org.hoohoot.homelab.manager.notifications.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.WeeklyReportNotificationBuilder
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.notifications.domain.ports.ReleaseCalendar
import org.hoohoot.homelab.manager.notifications.domain.ports.ViewingStats
import org.hoohoot.homelab.manager.shared.time.TimeService

@ApplicationScoped
class SendWeeklyReport(
    private val releaseCalendar: ReleaseCalendar,
    private val viewingStats: ViewingStats,
    private val sender: NotificationSender,
    private val timeService: TimeService,
) {
    suspend operator fun invoke() {
        try {
            Log.info("Sending weekly recap report")

            val nextWeek = timeService.getNextWeek()

            val movies = releaseCalendar.upcomingMovies(nextWeek)
            val episodes = releaseCalendar.upcomingEpisodes(nextWeek)
            val albums = releaseCalendar.upcomingAlbums(nextWeek)

            val topMovies = viewingStats.topMovies(lastDays = 7, limit = 3)
            val topSeries = viewingStats.topSeries(lastDays = 7, limit = 3)

            val notification = WeeklyReportNotificationBuilder(movies, episodes, albums, topMovies, topSeries).build()

            sender.send(NotificationRoom.MEDIA, notification)
            Log.info("Weekly recap report sent")
        } catch (e: Exception) {
            Log.error("Failed to send weekly recap report", e)
            throw e
        }
    }
}
