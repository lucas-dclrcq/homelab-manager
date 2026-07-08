package org.hoohoot.homelab.manager.notifications.domain.ports

import org.hoohoot.homelab.manager.shared.arr.lidarr.LidarrAlbum
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.shared.arr.sonarr.Episode
import org.hoohoot.homelab.manager.shared.time.Week

interface ReleaseCalendar {
    suspend fun upcomingMovies(week: Week): List<RadarrMovie>
    suspend fun upcomingEpisodes(week: Week): List<Episode>
    suspend fun upcomingAlbums(week: Week): List<LidarrAlbum>
}
