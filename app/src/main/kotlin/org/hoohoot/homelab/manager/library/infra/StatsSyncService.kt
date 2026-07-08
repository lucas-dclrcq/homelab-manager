package org.hoohoot.homelab.manager.library.infra

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.hoohoot.homelab.manager.library.infra.DiskUsage
import org.hoohoot.homelab.manager.library.infra.StatsSnapshotEntity
import org.hoohoot.homelab.manager.library.infra.StatsSnapshotRepository

@ApplicationScoped
class StatsSyncService(
    private val statsService: StatsService,
    private val statsSnapshotRepository: StatsSnapshotRepository,
) {
    suspend fun syncRadarr() = persist(statsService.collectRadarrStats())

    suspend fun syncSonarr() = persist(statsService.collectSonarrStats())

    suspend fun currentStats(): LibraryStats {
        val snapshots = statsSnapshotRepository.findAll()
        if (snapshots.isEmpty()) {
            return coroutineScope {
                val radarr = async { statsService.collectRadarrStats() }
                val sonarr = async { statsService.collectSonarrStats() }
                statsService.merge(listOf(radarr.await(), sonarr.await()))
            }
        }
        return statsService.merge(snapshots.map { it.toSourceStats() })
    }

    private suspend fun persist(stats: SourceStats) {
        statsSnapshotRepository.upsert(
            source = stats.source,
            movieCount = stats.movieCount,
            seriesCount = stats.seriesCount,
            episodeCount = stats.episodeCount,
            disks = stats.disks,
        )
    }

    private fun StatsSnapshotEntity.toSourceStats() = SourceStats(
        source = source,
        movieCount = movieCount ?: 0,
        seriesCount = seriesCount ?: 0,
        episodeCount = episodeCount ?: 0,
        disks = disks.map { (path, usage) -> DiskUsage(path, usage.freeBytes, usage.totalBytes) },
    )
}
