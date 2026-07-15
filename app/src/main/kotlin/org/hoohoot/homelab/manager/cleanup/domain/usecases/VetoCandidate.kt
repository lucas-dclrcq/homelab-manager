package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.VetoChannel
import org.hoohoot.homelab.manager.cleanup.domain.VetoResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class VetoCandidate(
    private val candidates: Candidates,
    private val protections: Protections,
) {
    suspend operator fun invoke(candidateId: UUID, username: String, via: VetoChannel): VetoResult {
        val candidate = candidates.find(candidateId) ?: return VetoResult.NotFound
        if (candidate.status != CleanupCandidateEntity.STATUS_PROPOSED) {
            return VetoResult.Invalid("ce candidat n'est plus en attente de veto")
        }

        val updated = candidates.update(candidateId) {
            it.status = CleanupCandidateEntity.STATUS_PROTECTED
            it.protectedBy = username
            it.protectedVia = via.name
            it.protectedAt = LocalDateTime.now()
        } ?: return VetoResult.NotFound

        saveProtectionIfMissing(updated, username)

        return VetoResult.Ok(updated)
    }

    private suspend fun saveProtectionIfMissing(candidate: CleanupCandidateEntity, username: String) {
        val alreadyCovered = protections.all().any {
            it.covers(candidate.radarrMovieId, candidate.sonarrSeriesId, candidate.seasonNumber)
        }
        if (alreadyCovered) return

        protections.save(
            CleanupProtectionEntity().apply {
                id = UUID.randomUUID()
                mediaKind = when (candidate.mediaKind) {
                    CleanupCandidateEntity.KIND_MOVIE -> CleanupProtectionEntity.KIND_MOVIE
                    else -> CleanupProtectionEntity.KIND_SEASON
                }
                radarrMovieId = candidate.radarrMovieId
                sonarrSeriesId = candidate.sonarrSeriesId
                seasonNumber = candidate.seasonNumber
                title = candidate.title
                year = candidate.year
                posterUrl = candidate.posterUrl
                protectedBy = username
                source = CleanupProtectionEntity.SOURCE_VETO
                createdAt = LocalDateTime.now()
            },
        )
    }
}
