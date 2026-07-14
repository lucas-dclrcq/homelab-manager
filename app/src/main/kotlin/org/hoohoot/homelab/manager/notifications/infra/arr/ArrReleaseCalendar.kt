package org.hoohoot.homelab.manager.notifications.infra.arr

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.domain.ports.ReleaseCalendar
import org.hoohoot.homelab.manager.shared.arr.lidarr.LidarrAlbum
import org.hoohoot.homelab.manager.shared.arr.lidarr.LidarrRestClient
import org.hoohoot.homelab.manager.shared.arr.lidarr.getAlbumCalendar
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.shared.arr.radarr.getMovieCalendar
import org.hoohoot.homelab.manager.shared.arr.sonarr.Episode
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.shared.arr.sonarr.getSeriesCalendar
import org.hoohoot.homelab.manager.shared.time.Week

@ApplicationScoped
class ArrReleaseCalendar(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
    @param:RestClient private val lidarrRestClient: LidarrRestClient,
) : ReleaseCalendar {

    override suspend fun upcomingMovies(week: Week): List<RadarrMovie> =
        radarrRestClient.getMovieCalendar(week.start.toJavaInstant(), week.end.toJavaInstant())

    override suspend fun upcomingEpisodes(week: Week): List<Episode> =
        sonarrRestClient.getSeriesCalendar(week.start.toJavaInstant(), week.end.toJavaInstant())

    override suspend fun upcomingAlbums(week: Week): List<LidarrAlbum> =
        lidarrRestClient.getAlbumCalendar(week.start.toJavaInstant(), week.end.toJavaInstant())
}

private fun kotlin.time.Instant.toJavaInstant(): java.time.Instant =
    java.time.Instant.ofEpochMilli(toEpochMilliseconds())
