package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class StatsSnapshotRepository {

    suspend fun upsert(
        source: String,
        movieCount: Int?,
        seriesCount: Int?,
        episodeCount: Int?,
        disks: List<DiskUsage>,
    ) {
        Panache.withTransaction {
            StatsSnapshotEntity.findById(source).chain { existing ->
                val entity = existing ?: StatsSnapshotEntity().also { it.source = source }
                entity.movieCount = movieCount
                entity.seriesCount = seriesCount
                entity.episodeCount = episodeCount
                entity.disks = disks.associate { it.path to DiskSpaceUsage(it.freeBytes, it.totalBytes) }
                entity.collectedAt = LocalDateTime.now()
                entity.persist<StatsSnapshotEntity>()
            }
        }.awaitSuspending()
    }

    suspend fun findAll(): List<StatsSnapshotEntity> =
        Panache.withSession { StatsSnapshotEntity.listAll() }.awaitSuspending()
}
