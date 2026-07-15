package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.RetryResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeasonEraser
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class RetryCandidate(
    private val candidates: Candidates,
    private val campaigns: Campaigns,
    private val protections: Protections,
    private val movieEraser: MovieEraser,
    private val seasonEraser: SeasonEraser,
) {
    suspend operator fun invoke(candidateId: UUID): RetryResult {
        val candidate = candidates.find(candidateId) ?: return RetryResult.NotFound
        if (candidate.status != CleanupCandidateEntity.STATUS_FAILED) {
            return RetryResult.Invalid("seul un candidat en échec peut être rejoué")
        }
        if (protections.all().any { it.covers(candidate.radarrMovieId, candidate.sonarrSeriesId, candidate.seasonNumber) }) {
            return RetryResult.Invalid("ce média a été protégé entre-temps")
        }

        val outcome = try {
            when (candidate.mediaKind) {
                CleanupCandidateEntity.KIND_MOVIE ->
                    movieEraser.deleteMovie(requireNotNull(candidate.radarrMovieId), candidate.sizeBytes)
                CleanupCandidateEntity.KIND_SEASON ->
                    seasonEraser.deleteSeason(requireNotNull(candidate.sonarrSeriesId), requireNotNull(candidate.seasonNumber))
                else -> DeleteOutcome.Failed("type de média inconnu : ${candidate.mediaKind}")
            }
        } catch (exception: Exception) {
            Log.error("Cleanup: retry failed for '${candidate.title}'", exception)
            DeleteOutcome.Failed(exception.message ?: exception.javaClass.simpleName)
        }

        return when (outcome) {
            is DeleteOutcome.Deleted -> deleted(candidate, outcome.freedBytes)
            DeleteOutcome.AlreadyGone -> deleted(candidate, 0)
            is DeleteOutcome.Failed -> {
                val updated = candidates.update(candidateId) { it.failureReason = outcome.reason }
                RetryResult.StillFailing(updated ?: candidate)
            }
        }
    }

    private suspend fun deleted(candidate: CleanupCandidateEntity, freedBytes: Long): RetryResult {
        val updated = candidates.update(requireNotNull(candidate.id)) {
            it.status = CleanupCandidateEntity.STATUS_DELETED
            it.deletedAt = LocalDateTime.now()
            it.freedBytes = freedBytes
            it.failureReason = null
        } ?: return RetryResult.NotFound

        if (freedBytes > 0) {
            campaigns.update(candidate.campaignId) { it.freedBytes += freedBytes }
        }

        return RetryResult.Ok(updated)
    }
}
