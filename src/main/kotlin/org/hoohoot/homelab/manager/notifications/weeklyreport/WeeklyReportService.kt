package org.hoohoot.homelab.manager.notifications.weeklyreport

import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.arr.lidarr.LidarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.lidarr.getAlbumCalendar
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.radarr.getMovieCalendar
import org.hoohoot.homelab.manager.notifications.arr.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.sonarr.getSeriesCalendar
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatMediaType
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatService
import org.hoohoot.homelab.manager.notifications.jellystat.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomProvider
import org.hoohoot.homelab.manager.notifications.matrix.sendNotification
import org.hoohoot.homelab.manager.time.TimeService

@ApplicationScoped
class WeeklyReportService(
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
    @param:RestClient private val radarrRestClient: RadarrRestClient,
    @param:RestClient private val lidarrRestClient: LidarrRestClient,
    private val jellystatService: JellystatService,
    private val matrixClient: MatrixClientServerApiClient,
    private val roomProvider: MatrixRoomProvider,
    private val timeService: TimeService
) {
    suspend fun sendWeeklyReport() {
        try {
            Log.info("Sending weekly recap report")

            val nextWeek = timeService.getNextWeek()

            val movies = radarrRestClient.getMovieCalendar(nextWeek.start, nextWeek.end)
            val episodes = sonarrRestClient.getSeriesCalendar(nextWeek.start, nextWeek.end)
            val albums = lidarrRestClient.getAlbumCalendar(nextWeek.start, nextWeek.end)

            val topMovies = jellystatService.getMostPopularByType(7, JellystatMediaType.Movie)
                .map { MostPopularMedia(it.name, it.uniqueViewers) }
                .sortedByDescending { it.uniqueViewers }
                .take(3)

            val topSeries = jellystatService.getMostPopularByType(7, JellystatMediaType.Series)
                .map { MostPopularMedia(it.name, it.uniqueViewers) }
                .sortedByDescending { it.uniqueViewers }
                .take(3)

            val notification = WeeklyReportNotificationBuilder(movies, episodes, albums, topMovies, topSeries).build()

            matrixClient.sendNotification(notification, roomProvider.media)
            Log.info("Weekly recap report sent")
        } catch (e: Exception) {
            Log.error("Failed to send weekly recap report", e)
            throw e
        }
    }
}
