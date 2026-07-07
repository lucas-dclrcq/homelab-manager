package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

data class DiskUsage(
    val path: String = "",
    val freeBytes: Long = 0,
    val totalBytes: Long = 0,
)

data class DiskSpaceUsage(
    val freeBytes: Long = 0,
    val totalBytes: Long = 0,
)

@Entity
@Table(name = "stats_snapshot")
class StatsSnapshotEntity : PanacheEntityBase {
    @Id
    @Column(name = "source")
    lateinit var source: String

    @Column(name = "movie_count")
    var movieCount: Int? = null

    @Column(name = "series_count")
    var seriesCount: Int? = null

    @Column(name = "episode_count")
    var episodeCount: Int? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "disks", nullable = false)
    var disks: Map<String, DiskSpaceUsage> = emptyMap()

    @Column(name = "collected_at", nullable = false)
    lateinit var collectedAt: LocalDateTime

    companion object : PanacheCompanionBase<StatsSnapshotEntity, String>
}
