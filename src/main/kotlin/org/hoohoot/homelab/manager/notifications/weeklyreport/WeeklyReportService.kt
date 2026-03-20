package org.hoohoot.homelab.manager.notifications.weeklyreport

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.media.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.radarr.getMovieCalendar
import org.hoohoot.homelab.manager.notifications.arr.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.sonarr.getSeriesCalendar
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatMediaType
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatService
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.time.TimeService

@ApplicationScoped
class WeeklyReportService(
    @RestClient private val sonarrRestClient: SonarrRestClient,
    @RestClient private val radarrRestClient: RadarrRestClient,
    private val jellystatService: JellystatService,
    private val matrixSender: MatrixNotificationSender,
    private val timeService: TimeService
) {
    suspend fun sendWeeklyReport() {
        Log.info("Sending weekly recap report")

        val currentWeek = timeService.getCurrentWeek()

        val movies = radarrRestClient.getMovieCalendar(currentWeek.start, currentWeek.end)
        val episodes = sonarrRestClient.getSeriesCalendar(currentWeek.start, currentWeek.end)

        val topMovies = jellystatService.getMostPopularByType(7, JellystatMediaType.Movie)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }
            .take(3)

        val topSeries = jellystatService.getMostPopularByType(7, JellystatMediaType.Series)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }
            .take(3)

        val notification = WeeklyReportNotificationBuilder(movies, episodes, topMovies, topSeries).build()

        matrixSender.sendMediaNotification(notification)
        Log.info("Weekly recap report sent")
    }
}
