package org.hoohoot.homelab.manager.problems.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime
import java.util.UUID

data class MediaSnapshot(
    val title: String? = null,
    val year: Int? = null,
    val posterUrl: String? = null,
    val overview: String? = null,
    val currentQuality: String? = null,
    val currentLanguages: List<String> = emptyList(),
    val desiredResolution: String? = null,
)

data class GrabbedRelease(
    val guid: String? = null,
    val indexerId: Int? = null,
    val indexer: String? = null,
    val title: String? = null,
    val quality: String? = null,
    val size: Long? = null,
)

data class ProblemWorkflowState(
    val media: MediaSnapshot? = null,
    val grabbedRelease: GrabbedRelease? = null,
    val description: String? = null,
)

@Entity
@Table(name = "problem_workflow")
class ProblemWorkflowEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "username", nullable = false)
    lateinit var username: String

    @Column(name = "media_type", nullable = false)
    lateinit var mediaType: String

    @Column(name = "problem_type")
    var problemType: String? = null

    @Column(name = "status", nullable = false)
    lateinit var status: String

    @Column(name = "radarr_movie_id")
    var radarrMovieId: Int? = null

    @Column(name = "sonarr_series_id")
    var sonarrSeriesId: Int? = null

    @Column(name = "media_title")
    var mediaTitle: String? = null

    @Column(name = "grabbed_at")
    var grabbedAt: LocalDateTime? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "state", nullable = false)
    var state: ProblemWorkflowState = ProblemWorkflowState()

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null

    @Column(name = "notification_event_id")
    var notificationEventId: String? = null

    companion object : PanacheCompanionBase<ProblemWorkflowEntity, UUID> {
        const val MEDIA_TYPE_MOVIE = "movie"
        const val MEDIA_TYPE_TV = "tv"

        const val PROBLEM_VO_SHOULD_BE_FRENCH = "vo_should_be_french"
        const val PROBLEM_OTHER = "other"

        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_AWAITING_IMPORT = "AWAITING_IMPORT"
        const val STATUS_REPORTED = "REPORTED"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_RESOLVED = "RESOLVED"
        const val STATUS_ABANDONED = "ABANDONED"
    }
}
