package org.hoohoot.homelab.manager.problems.api

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import org.hoohoot.homelab.manager.problems.domain.AnnotatedRelease
import org.hoohoot.homelab.manager.problems.domain.LibraryMovie
import org.hoohoot.homelab.manager.problems.domain.LibrarySeries
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.infra.GrabbedRelease
import org.hoohoot.homelab.manager.problems.infra.MediaSnapshot
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.api.notFound
import java.time.LocalDateTime
import java.util.UUID

data class ProblemMovieDto(
    val radarrMovieId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
)

data class ProblemSeriesDto(
    val sonarrSeriesId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
)

data class ProblemReleaseDto(
    val guid: String,
    val indexerId: Int,
    val indexer: String?,
    val title: String,
    val quality: String?,
    val size: Long?,
    val age: Int?,
    val seeders: Int?,
    val leechers: Int?,
    val protocol: String?,
    val rejected: Boolean,
    val rejections: List<String>,
    // Jackson retire le préfixe "is" des booléens Kotlin (isFrench -> "french"), ce qui casse
    // le contrat OpenAPI/Orval (isFrench/isRecommended). On force le nom exposé.
    @get:JsonProperty("isFrench") val isFrench: Boolean,
    @get:JsonProperty("isRecommended") val isRecommended: Boolean,
)

data class ProblemWorkflowDto(
    val id: UUID,
    val mediaType: String,
    val status: String,
    val currentStep: String,
    val problemType: String?,
    val description: String?,
    val media: MediaSnapshot?,
    val grabbedRelease: GrabbedRelease?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
)

data class AdminProblemWorkflowDto(
    val username: String,
    val workflow: ProblemWorkflowDto,
)

data class CreateWorkflowRequest(val mediaType: String?)
data class SelectMovieRequest(@field:NotNull val radarrMovieId: Int?)
data class SelectSeriesRequest(@field:NotNull val sonarrSeriesId: Int?)
data class SelectProblemRequest(val problemType: String?, val description: String?)
data class GrabReleaseRequest(
    @field:NotNull val guid: String?,
    @field:NotNull val indexerId: Int?,
    val title: String?,
    val indexer: String?,
    val quality: String?,
    val size: Long?,
)

internal const val GRAB_FAILED_MESSAGE =
    "Radarr a refusé le téléchargement, la release n'est peut-être plus disponible"

internal fun ProblemResult.toResponse(okStatus: Response.Status = Response.Status.OK): Response = when (this) {
    is ProblemResult.Ok -> Response.status(okStatus).entity(workflow.toDto()).build()
    ProblemResult.NotFound -> notFound()
    is ProblemResult.Invalid -> badRequest(message)
    is ProblemResult.Conflict -> conflict(message)
    ProblemResult.GrabFailed ->
        Response.status(Response.Status.BAD_GATEWAY).entity(mapOf("error" to GRAB_FAILED_MESSAGE)).build()
}

internal fun ProblemWorkflowEntity.toDto() = ProblemWorkflowDto(
    id = requireNotNull(id),
    mediaType = mediaType,
    status = status,
    currentStep = currentStep(),
    problemType = problemType,
    description = state.description,
    media = state.media,
    grabbedRelease = state.grabbedRelease,
    createdAt = createdAt,
    updatedAt = updatedAt,
    completedAt = completedAt,
)

internal fun ProblemWorkflowEntity.toAdminDto() = AdminProblemWorkflowDto(
    username = username,
    workflow = toDto(),
)

private fun ProblemWorkflowEntity.currentStep(): String = when {
    status == ProblemWorkflowEntity.STATUS_ABANDONED -> "ABANDONED"
    status == ProblemWorkflowEntity.STATUS_COMPLETED -> "COMPLETED"
    status == ProblemWorkflowEntity.STATUS_RESOLVED -> "RESOLVED"
    status == ProblemWorkflowEntity.STATUS_REPORTED -> "REPORTED"
    status == ProblemWorkflowEntity.STATUS_AWAITING_IMPORT -> "AWAITING_IMPORT"
    radarrMovieId == null && sonarrSeriesId == null -> "SELECT_MEDIA"
    problemType == null -> "SELECT_PROBLEM"
    else -> "SELECT_RELEASE"
}

internal fun LibraryMovie.toMovieDto() = ProblemMovieDto(
    radarrMovieId = radarrMovieId,
    title = title,
    year = year,
    posterUrl = posterUrl,
)

internal fun LibrarySeries.toSeriesDto() = ProblemSeriesDto(
    sonarrSeriesId = sonarrSeriesId,
    title = title,
    year = year,
    posterUrl = posterUrl,
)

internal fun AnnotatedRelease.toReleaseDto() = ProblemReleaseDto(
    guid = release.guid,
    indexerId = release.indexerId,
    indexer = release.indexer,
    title = release.title,
    quality = release.quality,
    size = release.size,
    age = release.age,
    seeders = release.seeders,
    leechers = release.leechers,
    protocol = release.protocol,
    rejected = release.rejected,
    rejections = release.rejections,
    isFrench = isFrench,
    isRecommended = isRecommended,
)
