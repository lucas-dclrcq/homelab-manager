package org.hoohoot.homelab.manager.library.infra

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.library.infra.DiskUsage

const val SOURCE_RADARR = "radarr"
const val SOURCE_SONARR = "sonarr"

data class LibraryStats(
    val movieCount: Int,
    val seriesCount: Int,
    val episodeCount: Int,
    val diskUsedBytes: Long,
    val diskFreeBytes: Long,
    val diskTotalBytes: Long,
)

data class SourceStats(
    val source: String,
    val movieCount: Int,
    val seriesCount: Int,
    val episodeCount: Int,
    val disks: List<DiskUsage>,
)

@ApplicationScoped
class StatsService(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
) {
    suspend fun collectRadarrStats(): SourceStats = coroutineScope {
        val movies = async { radarrRestClient.getMovies().orEmpty() }
        val diskSpace = async { radarrRestClient.getDiskSpace().orEmpty() }

        SourceStats(
            source = SOURCE_RADARR,
            movieCount = movies.await().size,
            seriesCount = 0,
            episodeCount = 0,
            disks = diskSpace.await()
                .filter { it.path != null && it.totalSpace != null }
                .map { DiskUsage(path = it.path!!, freeBytes = it.freeSpace ?: 0, totalBytes = it.totalSpace ?: 0) },
        )
    }

    suspend fun collectSonarrStats(): SourceStats = coroutineScope {
        val series = async { sonarrRestClient.getSeries().orEmpty() }
        val diskSpace = async { sonarrRestClient.getDiskSpace().orEmpty() }

        SourceStats(
            source = SOURCE_SONARR,
            movieCount = 0,
            seriesCount = series.await().size,
            episodeCount = series.await().sumOf { it.statistics?.episodeFileCount ?: 0 },
            disks = diskSpace.await()
                .filter { it.path != null && it.totalSpace != null }
                .map { DiskUsage(path = it.path!!, freeBytes = it.freeSpace ?: 0, totalBytes = it.totalSpace ?: 0) },
        )
    }

    fun merge(sources: List<SourceStats>): LibraryStats {
        val disks = sources.flatMap { it.disks }.distinctBy { it.path }
        val diskTotal = disks.sumOf { it.totalBytes }
        val diskFree = disks.sumOf { it.freeBytes }

        return LibraryStats(
            movieCount = sources.sumOf { it.movieCount },
            seriesCount = sources.sumOf { it.seriesCount },
            episodeCount = sources.sumOf { it.episodeCount },
            diskUsedBytes = diskTotal - diskFree,
            diskFreeBytes = diskFree,
            diskTotalBytes = diskTotal,
        )
    }
}
