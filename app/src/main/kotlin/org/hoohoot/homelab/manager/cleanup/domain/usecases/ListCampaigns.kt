package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity

data class CampaignWithCandidates(
    val campaign: CleanupCampaignEntity,
    val candidates: List<CleanupCandidateEntity>,
)

@ApplicationScoped
class ListCampaigns(
    private val campaigns: Campaigns,
    private val candidates: Candidates,
) {
    suspend operator fun invoke(): List<CampaignWithCandidates> =
        campaigns.listAll().map {
            CampaignWithCandidates(it, candidates.listByCampaign(requireNotNull(it.id)))
        }
}
