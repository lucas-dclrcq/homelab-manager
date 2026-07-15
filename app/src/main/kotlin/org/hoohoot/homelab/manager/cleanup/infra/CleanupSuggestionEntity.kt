package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "cleanup_suggestion")
class CleanupSuggestionEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

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

    @Column(name = "suggested_by", nullable = false)
    lateinit var suggestedBy: String

    @Column(name = "announcement_event_id")
    var announcementEventId: String? = null

    @Column(name = "status", nullable = false)
    lateinit var status: String

    @Column(name = "delete_after", nullable = false)
    lateinit var deleteAfter: LocalDateTime

    @Column(name = "vetoed_by")
    var vetoedBy: String? = null

    @Column(name = "vetoed_at")
    var vetoedAt: LocalDateTime? = null

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

    fun overlaps(mediaKind: String, radarrMovieId: Int?, sonarrSeriesId: Int?, seasonNumber: Int?): Boolean =
        when (this.mediaKind) {
            KIND_MOVIE -> mediaKind == KIND_MOVIE && radarrMovieId != null && this.radarrMovieId == radarrMovieId
            else -> sonarrSeriesId != null && this.sonarrSeriesId == sonarrSeriesId &&
                (this.mediaKind == KIND_SERIES || mediaKind == KIND_SERIES || this.seasonNumber == seasonNumber)
        }

    companion object : PanacheCompanionBase<CleanupSuggestionEntity, UUID> {
        const val KIND_MOVIE = "MOVIE"
        const val KIND_SERIES = "SERIES"
        const val KIND_SEASON = "SEASON"

        const val STATUS_PENDING = "PENDING"
        const val STATUS_VETOED = "VETOED"
        const val STATUS_DELETED = "DELETED"
        const val STATUS_SKIPPED = "SKIPPED"
        const val STATUS_FAILED = "FAILED"
    }
}
