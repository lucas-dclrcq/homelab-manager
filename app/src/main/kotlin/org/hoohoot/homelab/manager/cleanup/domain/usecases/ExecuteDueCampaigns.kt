package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.ExecutionSummary
import org.hoohoot.homelab.manager.cleanup.domain.ports.ActiveProblems
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.domain.ports.DiskSpaceGauge
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeasonEraser
import org.hoohoot.homelab.manager.cleanup.domain.CampaignState
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import java.time.LocalDateTime

@ApplicationScoped
class ExecuteDueCampaigns(
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val protections: Protections,
    private val activeProblems: ActiveProblems,
    private val movieEraser: MovieEraser,
    private val seasonEraser: SeasonEraser,
    private val diskSpaceGauge: DiskSpaceGauge,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(): List<ExecutionSummary> =
        campaigns.listDue(LocalDateTime.now()).map { execute(it) }

    private suspend fun execute(campaign: CleanupCampaignEntity): ExecutionSummary {
        val campaignId = requireNotNull(campaign.id)
        val all = candidates.listByCampaign(campaignId)
        val proposed = all.filter { it.status == CleanupCandidateEntity.STATUS_PROPOSED }
            .sortedByDescending { it.score }

        val liveFree = diskSpaceGauge.liveFree(campaign.diskPath) ?: diskSpaceGauge.snapshotFree(campaign.diskPath)
        val targetFreeBytes = campaign.freeBytesAtStart + campaign.targetBytesToFree
        val stillToFree = liveFree?.let { targetFreeBytes - it } ?: campaign.targetBytesToFree

        if (stillToFree <= 0) {
            proposed.forEach { skip(it, "l'espace disque s'est libéré entre-temps") }
            return complete(campaign, freedNow = 0, note = "Espace libéré entre-temps, aucune suppression")
        }

        val problemIds = activeProblems.activeMediaIds()
        var freed = 0L
        for (candidate in proposed) {
            if (freed >= stillToFree) {
                skip(candidate, "objectif d'espace atteint")
                continue
            }

            val candidateId = requireNotNull(candidate.id)
            val fresh = candidates.find(candidateId) ?: continue
            if (fresh.status != CleanupCandidateEntity.STATUS_PROPOSED) continue
            if (protections.all().any { it.covers(fresh.radarrMovieId, fresh.sonarrSeriesId, fresh.seasonNumber) }) {
                skip(fresh, "protégé pendant la période de grâce")
                continue
            }
            if (fresh.radarrMovieId in problemIds.radarrMovieIds ||
                fresh.sonarrSeriesId in problemIds.sonarrSeriesIds
            ) {
                skip(fresh, "un problème est en cours sur ce média")
                continue
            }

            when (val outcome = delete(fresh)) {
                is DeleteOutcome.Deleted -> {
                    freed += outcome.freedBytes
                    candidates.update(candidateId) {
                        it.status = CleanupCandidateEntity.STATUS_DELETED
                        it.deletedAt = LocalDateTime.now()
                        it.freedBytes = outcome.freedBytes
                    }
                }
                DeleteOutcome.AlreadyGone -> candidates.update(candidateId) {
                    it.status = CleanupCandidateEntity.STATUS_DELETED
                    it.deletedAt = LocalDateTime.now()
                    it.freedBytes = 0
                    it.failureReason = "déjà absent de la bibliothèque"
                }
                is DeleteOutcome.Failed -> candidates.update(candidateId) {
                    it.status = CleanupCandidateEntity.STATUS_FAILED
                    it.failureReason = outcome.reason
                }
            }
        }

        return complete(campaign, freedNow = freed, note = null)
    }

    private suspend fun delete(candidate: CleanupCandidateEntity): DeleteOutcome = try {
        when (candidate.mediaKind) {
            CleanupCandidateEntity.KIND_MOVIE ->
                movieEraser.deleteMovie(requireNotNull(candidate.radarrMovieId), candidate.sizeBytes)
            CleanupCandidateEntity.KIND_SEASON ->
                seasonEraser.deleteSeason(requireNotNull(candidate.sonarrSeriesId), requireNotNull(candidate.seasonNumber))
            else -> DeleteOutcome.Failed("type de média inconnu : ${candidate.mediaKind}")
        }
    } catch (exception: Exception) {
        Log.error("Cleanup: deletion failed for '${candidate.title}'", exception)
        DeleteOutcome.Failed(exception.message ?: exception.javaClass.simpleName)
    }

    private suspend fun skip(candidate: CleanupCandidateEntity, reason: String) {
        candidates.update(requireNotNull(candidate.id)) {
            it.status = CleanupCandidateEntity.STATUS_SKIPPED
            it.failureReason = reason
        }
    }

    private suspend fun complete(
        campaign: CleanupCampaignEntity,
        freedNow: Long,
        note: String?,
    ): ExecutionSummary {
        val refreshed = candidates.listByCampaign(requireNotNull(campaign.id))
        val summary = ExecutionSummary(
            deletedCount = refreshed.count { it.status == CleanupCandidateEntity.STATUS_DELETED },
            protectedCount = refreshed.count { it.status == CleanupCandidateEntity.STATUS_PROTECTED },
            skippedCount = refreshed.count { it.status == CleanupCandidateEntity.STATUS_SKIPPED },
            failedCount = refreshed.count { it.status == CleanupCandidateEntity.STATUS_FAILED },
            freedBytes = freedNow,
            finishedAt = LocalDateTime.now(),
            note = note,
        )

        val updated = campaigns.update(requireNotNull(campaign.id)) {
            it.status = CleanupCampaignEntity.STATUS_COMPLETED
            it.freedBytes = freedNow
            it.completedAt = LocalDateTime.now()
            it.state = CampaignState(executionSummary = summary)
        }

        try {
            notifier.announceExecution(updated ?: campaign, summary)
        } catch (exception: Exception) {
            Log.error("Cleanup: execution summary announcement failed", exception)
        }

        return summary
    }
}
