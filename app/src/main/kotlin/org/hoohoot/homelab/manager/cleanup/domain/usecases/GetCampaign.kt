package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import java.util.UUID

@ApplicationScoped
class GetCampaign(
    private val campaigns: Campaigns,
    private val candidates: Candidates,
) {
    suspend operator fun invoke(id: UUID): CampaignWithCandidates? {
        val campaign = campaigns.find(id) ?: return null
        return CampaignWithCandidates(campaign, candidates.listByCampaign(id))
    }
}
