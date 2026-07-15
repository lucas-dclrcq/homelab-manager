package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ProtectResult
import org.hoohoot.homelab.manager.cleanup.domain.VetoChannel
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesCatalog
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import java.time.LocalDateTime
import java.util.UUID

data class ProtectionRequest(
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
)

@ApplicationScoped
class ProtectMedia(
    private val protections: Protections,
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val movieCatalog: MovieCatalog,
    private val seriesCatalog: SeriesCatalog,
) {
    suspend operator fun invoke(username: String, request: ProtectionRequest): ProtectResult {
        val protection = when (request.mediaKind) {
            CleanupProtectionEntity.KIND_MOVIE -> movieProtection(request)
            CleanupProtectionEntity.KIND_SERIES, CleanupProtectionEntity.KIND_SEASON -> seriesProtection(request)
            else -> return ProtectResult.Invalid("type de média inconnu : ${request.mediaKind}")
        } ?: return ProtectResult.MediaNotFound

        val existing = protections.all().firstOrNull {
            it.covers(protection.radarrMovieId, protection.sonarrSeriesId, protection.seasonNumber)
        }
        if (existing != null) return ProtectResult.AlreadyProtected(existing)

        protection.protectedBy = username
        protection.source = CleanupProtectionEntity.SOURCE_PROACTIVE
        protection.createdAt = LocalDateTime.now()
        val saved = protections.save(protection)

        protectMatchingCandidates(saved, username)

        return ProtectResult.Ok(saved)
    }

    private suspend fun movieProtection(request: ProtectionRequest): CleanupProtectionEntity? {
        val movieId = request.radarrMovieId ?: return null
        val movie = movieCatalog.allMovies().firstOrNull { it.radarrMovieId == movieId } ?: return null
        return CleanupProtectionEntity().apply {
            id = UUID.randomUUID()
            mediaKind = CleanupProtectionEntity.KIND_MOVIE
            radarrMovieId = movie.radarrMovieId
            title = movie.title
            year = movie.year
            posterUrl = movie.posterUrl
        }
    }

    private suspend fun seriesProtection(request: ProtectionRequest): CleanupProtectionEntity? {
        val seriesId = request.sonarrSeriesId ?: return null
        val series = seriesCatalog.allSeries().firstOrNull { it.sonarrSeriesId == seriesId } ?: return null
        if (request.mediaKind == CleanupProtectionEntity.KIND_SEASON) {
            val seasonNumber = request.seasonNumber ?: return null
            if (series.seasons.none { it.seasonNumber == seasonNumber }) return null
        }
        return CleanupProtectionEntity().apply {
            id = UUID.randomUUID()
            mediaKind = request.mediaKind
            sonarrSeriesId = series.sonarrSeriesId
            seasonNumber = request.seasonNumber.takeIf { request.mediaKind == CleanupProtectionEntity.KIND_SEASON }
            title = series.title
            year = series.year
            posterUrl = series.posterUrl
        }
    }

    private suspend fun protectMatchingCandidates(protection: CleanupProtectionEntity, username: String) {
        val campaign = campaigns.findActive() ?: return
        candidates.listByCampaign(requireNotNull(campaign.id))
            .filter { it.status == CleanupCandidateEntity.STATUS_PROPOSED }
            .filter { protection.covers(it.radarrMovieId, it.sonarrSeriesId, it.seasonNumber) }
            .forEach { candidate ->
                candidates.update(requireNotNull(candidate.id)) {
                    it.status = CleanupCandidateEntity.STATUS_PROTECTED
                    it.protectedBy = username
                    it.protectedVia = VetoChannel.WEB.name
                    it.protectedAt = LocalDateTime.now()
                }
            }
    }
}
