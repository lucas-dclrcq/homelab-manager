package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.hoohoot.homelab.manager.cleanup.domain.ScoreBreakdown
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "cleanup_candidate")
class CleanupCandidateEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "campaign_id", nullable = false)
    lateinit var campaignId: UUID

    @Column(name = "media_kind", nullable = false)
    lateinit var mediaKind: String

    @Column(name = "radarr_movie_id")
    var radarrMovieId: Int? = null

    @Column(name = "sonarr_series_id")
    var sonarrSeriesId: Int? = null

    @Column(name = "season_number")
    var seasonNumber: Int? = null

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @Column(name = "year")
    var year: Int? = null

    @Column(name = "poster_url")
    var posterUrl: String? = null

    @Column(name = "size_bytes", nullable = false)
    var sizeBytes: Long = 0

    @Column(name = "requester")
    var requester: String? = null

    @Column(name = "score", nullable = false)
    var score: BigDecimal = BigDecimal.ZERO

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown", nullable = false)
    var scoreBreakdown: ScoreBreakdown = ScoreBreakdown()

    @Column(name = "status", nullable = false)
    lateinit var status: String

    @Column(name = "protected_by")
    var protectedBy: String? = null

    @Column(name = "protected_via")
    var protectedVia: String? = null

    @Column(name = "protected_at")
    var protectedAt: LocalDateTime? = null

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null

    @Column(name = "freed_bytes")
    var freedBytes: Long? = null

    @Column(name = "failure_reason")
    var failureReason: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    fun displayTitle(): String = if (seasonNumber != null) "$title — Saison $seasonNumber" else title

    companion object : PanacheCompanionBase<CleanupCandidateEntity, UUID> {
        const val KIND_MOVIE = "MOVIE"
        const val KIND_SEASON = "SEASON"

        const val STATUS_PROPOSED = "PROPOSED"
        const val STATUS_PROTECTED = "PROTECTED"
        const val STATUS_DELETED = "DELETED"
        const val STATUS_SKIPPED = "SKIPPED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
