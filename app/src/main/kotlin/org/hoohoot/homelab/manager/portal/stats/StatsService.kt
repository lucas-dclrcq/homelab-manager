package org.hoohoot.homelab.manager.portal.stats

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.notifications.arr.sonarr.SonarrRestClient

data class LibraryStats(
    val movieCount: Int,
    val seriesCount: Int,
    val episodeCount: Int,
    val diskUsedBytes: Long,
    val diskFreeBytes: Long,
    val diskTotalBytes: Long,
)

@ApplicationScoped
class StatsService(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
) {
    suspend fun collectStats(): LibraryStats = coroutineScope {
        val movies = async { radarrRestClient.getMovies().orEmpty() }
        val series = async { sonarrRestClient.getSeries().orEmpty() }
        val radarrDiskSpace = async { radarrRestClient.getDiskSpace().orEmpty() }
        val sonarrDiskSpace = async { sonarrRestClient.getDiskSpace().orEmpty() }

        // Radarr and Sonarr usually report the same mounts: dedupe by path before summing
        val disks = (radarrDiskSpace.await() + sonarrDiskSpace.await())
            .filter { it.path != null && it.totalSpace != null }
            .distinctBy { it.path }

        val diskTotal = disks.sumOf { it.totalSpace ?: 0 }
        val diskFree = disks.sumOf { it.freeSpace ?: 0 }

        LibraryStats(
            movieCount = movies.await().size,
            seriesCount = series.await().size,
            episodeCount = series.await().sumOf { it.statistics?.episodeFileCount ?: 0 },
            diskUsedBytes = diskTotal - diskFree,
            diskFreeBytes = diskFree,
            diskTotalBytes = diskTotal,
        )
    }
}
