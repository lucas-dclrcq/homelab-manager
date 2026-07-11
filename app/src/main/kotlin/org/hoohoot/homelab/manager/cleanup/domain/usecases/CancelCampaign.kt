package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.CampaignActionResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CancelCampaign(
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(campaignId: UUID): CampaignActionResult {
        val campaign = campaigns.find(campaignId) ?: return CampaignActionResult.NotFound
        if (campaign.status != CleanupCampaignEntity.STATUS_ANNOUNCED) {
            return CampaignActionResult.Invalid("cette campagne n'est plus en cours")
        }

        candidates.listByCampaign(campaignId)
            .filter { it.status == CleanupCandidateEntity.STATUS_PROPOSED }
            .forEach { candidate ->
                candidates.update(requireNotNull(candidate.id)) {
                    it.status = CleanupCandidateEntity.STATUS_CANCELLED
                }
            }

        val updated = campaigns.update(campaignId) {
            it.status = CleanupCampaignEntity.STATUS_CANCELLED
            it.completedAt = LocalDateTime.now()
        } ?: return CampaignActionResult.NotFound

        try {
            notifier.announceCancellation(updated)
        } catch (exception: Exception) {
            Log.error("Cleanup: cancellation announcement failed", exception)
        }

        return CampaignActionResult.Ok(updated)
    }
}
