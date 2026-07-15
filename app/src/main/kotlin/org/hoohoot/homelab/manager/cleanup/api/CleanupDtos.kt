package org.hoohoot.homelab.manager.cleanup.api

import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.core.Response
import org.hoohoot.homelab.manager.cleanup.domain.CampaignActionResult
import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.ExecutionSummary
import org.hoohoot.homelab.manager.cleanup.domain.ProtectResult
import org.hoohoot.homelab.manager.cleanup.domain.RetryResult
import org.hoohoot.homelab.manager.cleanup.domain.ScoreBreakdown
import org.hoohoot.homelab.manager.cleanup.domain.SuggestResult
import org.hoohoot.homelab.manager.cleanup.domain.UnprotectResult
import org.hoohoot.homelab.manager.cleanup.domain.VetoResult
import org.hoohoot.homelab.manager.cleanup.domain.usecases.CampaignWithCandidates
import org.hoohoot.homelab.manager.cleanup.domain.usecases.EffectiveCleanupConfig
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.api.notFound
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

data class CleanupOverviewDto(
    val campaign: CleanupCampaignDetailsDto?,
    val diskPath: String,
    val diskFreeBytes: Long?,
    val thresholdBytes: Long,
    val targetFreeBytes: Long,
)

data class CleanupCampaignDetailsDto(
    val id: UUID,
    val status: String,
    val triggerType: String,
    val diskPath: String,
    val freeBytesAtStart: Long,
    val targetBytesToFree: Long,
    val freedBytes: Long,
    val graceEndsAt: OffsetDateTime,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val executionSummary: ExecutionSummary?,
    val candidates: List<CleanupCandidateDto>,
)

data class CleanupCandidateDto(
    val id: UUID,
    val mediaKind: String,
    val title: String,
    val displayTitle: String,
    val year: Int?,
    val posterUrl: String?,
    val seasonNumber: Int?,
    val sizeBytes: Long,
    val requester: String?,
    val status: String,
    val score: Double,
    val scoreBreakdown: ScoreBreakdown,
    val protectedBy: String?,
    val protectedVia: String?,
    val deletedAt: LocalDateTime?,
    val freedBytes: Long?,
    val failureReason: String?,
)

data class CleanupCampaignSummaryDto(
    val id: UUID,
    val status: String,
    val triggerType: String,
    val createdAt: LocalDateTime,
    val graceEndsAt: OffsetDateTime,
    val completedAt: LocalDateTime?,
    val targetBytesToFree: Long,
    val freedBytes: Long,
    val candidateCount: Int,
    val deletedCount: Int,
    val protectedCount: Int,
    val skippedCount: Int,
    val failedCount: Int,
)

data class CleanupProtectionDto(
    val id: UUID,
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val protectedBy: String,
    val source: String,
    val createdAt: LocalDateTime,
)

data class CleanupMediaDto(
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
    val seasonNumbers: List<Int>,
)

data class CleanupConfigDto(
    val diskPath: String,
    val thresholdBytes: Long,
    val targetFreeBytes: Long,
    val graceDays: Long,
    val suggestionGraceDays: Long,
    val minAgeDays: Long,
    val recentSeriesWatchDays: Long,
    val inProgressDays: Long,
    val maxCandidates: Int,
    val minScore: Double,
    val weightLastWatched: Double,
    val weightDownloadAge: Double,
    val weightSize: Double,
    val weightCompletion: Double,
    val weightRequesterActivity: Double,
    val knownDiskPaths: List<String>,
    val diskFreeBytes: Long?,
)

data class CreateProtectionRequest(
    @field:NotNull val mediaKind: String?,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
)

data class CleanupSuggestionDto(
    val id: UUID,
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
    val title: String,
    val displayTitle: String,
    val year: Int?,
    val posterUrl: String?,
    val sizeBytes: Long,
    val suggestedBy: String,
    val status: String,
    val deleteAfter: OffsetDateTime,
    val vetoedBy: String?,
    val freedBytes: Long?,
    val failureReason: String?,
    val createdAt: LocalDateTime,
)

data class CreateSuggestionRequest(
    @field:NotNull val mediaKind: String?,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
)

data class ForceScanRequest(val targetBytes: Long?)

data class ScanStartedDto(val jobIdentity: String)

internal fun CleanupCampaignEntity.toDetailsDto(candidates: List<CleanupCandidateEntity>) =
    CleanupCampaignDetailsDto(
        id = requireNotNull(id),
        status = status,
        triggerType = triggerType,
        diskPath = diskPath,
        freeBytesAtStart = freeBytesAtStart,
        targetBytesToFree = targetBytesToFree,
        freedBytes = freedBytes,
        graceEndsAt = graceEndsAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
        createdAt = createdAt,
        completedAt = completedAt,
        executionSummary = state.executionSummary,
        candidates = candidates.map { it.toDto() },
    )

internal fun CleanupCandidateEntity.toDto() = CleanupCandidateDto(
    id = requireNotNull(id),
    mediaKind = mediaKind,
    title = title,
    displayTitle = displayTitle(),
    year = year,
    posterUrl = posterUrl,
    seasonNumber = seasonNumber,
    sizeBytes = sizeBytes,
    requester = requester,
    status = status,
    score = score.toDouble(),
    scoreBreakdown = scoreBreakdown,
    protectedBy = protectedBy,
    protectedVia = protectedVia,
    deletedAt = deletedAt,
    freedBytes = freedBytes,
    failureReason = failureReason,
)

internal fun CampaignWithCandidates.toSummaryDto() = CleanupCampaignSummaryDto(
    id = requireNotNull(campaign.id),
    status = campaign.status,
    triggerType = campaign.triggerType,
    createdAt = campaign.createdAt,
    graceEndsAt = campaign.graceEndsAt.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
    completedAt = campaign.completedAt,
    targetBytesToFree = campaign.targetBytesToFree,
    freedBytes = campaign.freedBytes,
    candidateCount = candidates.size,
    deletedCount = candidates.count { it.status == CleanupCandidateEntity.STATUS_DELETED },
    protectedCount = candidates.count { it.status == CleanupCandidateEntity.STATUS_PROTECTED },
    skippedCount = candidates.count { it.status == CleanupCandidateEntity.STATUS_SKIPPED },
    failedCount = candidates.count { it.status == CleanupCandidateEntity.STATUS_FAILED },
)

internal fun CleanupProtectionEntity.toDto() = CleanupProtectionDto(
    id = requireNotNull(id),
    mediaKind = mediaKind,
    radarrMovieId = radarrMovieId,
    sonarrSeriesId = sonarrSeriesId,
    seasonNumber = seasonNumber,
    title = title,
    year = year,
    posterUrl = posterUrl,
    protectedBy = protectedBy,
    source = source,
    createdAt = createdAt,
)

internal fun CleanupMovie.toMediaDto() = CleanupMediaDto(
    mediaKind = CleanupProtectionEntity.KIND_MOVIE,
    radarrMovieId = radarrMovieId,
    sonarrSeriesId = null,
    title = title,
    year = year,
    posterUrl = posterUrl,
    seasonNumbers = emptyList(),
)

internal fun CleanupSeries.toMediaDto() = CleanupMediaDto(
    mediaKind = CleanupProtectionEntity.KIND_SERIES,
    radarrMovieId = null,
    sonarrSeriesId = sonarrSeriesId,
    title = title,
    year = year,
    posterUrl = posterUrl,
    seasonNumbers = seasons.map { it.seasonNumber }.sorted(),
)

internal fun EffectiveCleanupConfig.toDto() = CleanupConfigDto(
    diskPath = config.diskPath,
    thresholdBytes = config.thresholdFreeBytes,
    targetFreeBytes = config.targetFreeBytes,
    graceDays = config.graceDays,
    suggestionGraceDays = config.suggestionGraceDays,
    minAgeDays = config.minAgeDays,
    recentSeriesWatchDays = config.recentSeriesWatchDays,
    inProgressDays = config.inProgressDays,
    maxCandidates = config.maxCandidates,
    minScore = config.minScore,
    weightLastWatched = config.scoring.weightLastWatched,
    weightDownloadAge = config.scoring.weightDownloadAge,
    weightSize = config.scoring.weightSize,
    weightCompletion = config.scoring.weightCompletion,
    weightRequesterActivity = config.scoring.weightRequesterActivity,
    knownDiskPaths = knownDiskPaths,
    diskFreeBytes = diskFreeBytes,
)

internal fun CleanupSuggestionEntity.toDto() = CleanupSuggestionDto(
    id = requireNotNull(id),
    mediaKind = mediaKind,
    radarrMovieId = radarrMovieId,
    sonarrSeriesId = sonarrSeriesId,
    seasonNumber = seasonNumber,
    title = title,
    displayTitle = displayTitle(),
    year = year,
    posterUrl = posterUrl,
    sizeBytes = sizeBytes,
    suggestedBy = suggestedBy,
    status = status,
    deleteAfter = deleteAfter.atZone(ZoneId.systemDefault()).toOffsetDateTime(),
    vetoedBy = vetoedBy,
    freedBytes = freedBytes,
    failureReason = failureReason,
    createdAt = createdAt,
)

internal fun SuggestResult.toResponse(): Response = when (this) {
    is SuggestResult.Ok -> Response.status(Response.Status.CREATED).entity(suggestion.toDto()).build()
    is SuggestResult.AlreadySuggested -> conflict("« ${suggestion.title} » est déjà proposé à la suppression")
    is SuggestResult.ProtectedMedia -> conflict("« ${protection.title} » est protégé, impossible de proposer sa suppression")
    is SuggestResult.Invalid -> badRequest(message)
    SuggestResult.MediaNotFound -> notFound("média introuvable dans la bibliothèque")
}

internal fun VetoResult.toResponse(): Response = when (this) {
    is VetoResult.Ok -> Response.ok(candidate.toDto()).build()
    VetoResult.NotFound -> notFound()
    is VetoResult.Invalid -> conflict(message)
}

internal fun ProtectResult.toResponse(): Response = when (this) {
    is ProtectResult.Ok -> Response.status(Response.Status.CREATED).entity(protection.toDto()).build()
    is ProtectResult.AlreadyProtected -> conflict("« ${protection.title} » est déjà protégé")
    is ProtectResult.Invalid -> badRequest(message)
    ProtectResult.MediaNotFound -> notFound("média introuvable dans la bibliothèque")
}

internal fun UnprotectResult.toResponse(): Response = when (this) {
    UnprotectResult.Ok -> Response.noContent().build()
    UnprotectResult.NotFound -> notFound()
    UnprotectResult.Forbidden -> Response.status(Response.Status.FORBIDDEN)
        .entity(mapOf("error" to "seul l'auteur d'une protection peut la retirer")).build()
}

internal fun CampaignActionResult.toResponse(candidates: List<CleanupCandidateEntity>): Response = when (this) {
    is CampaignActionResult.Ok -> Response.ok(campaign.toDetailsDto(candidates)).build()
    CampaignActionResult.NotFound -> notFound()
    is CampaignActionResult.Invalid -> conflict(message)
}

internal fun RetryResult.toResponse(): Response = when (this) {
    is RetryResult.Ok -> Response.ok(candidate.toDto()).build()
    RetryResult.NotFound -> notFound()
    is RetryResult.Invalid -> conflict(message)
    is RetryResult.StillFailing -> Response.status(Response.Status.BAD_GATEWAY).entity(candidate.toDto()).build()
}
