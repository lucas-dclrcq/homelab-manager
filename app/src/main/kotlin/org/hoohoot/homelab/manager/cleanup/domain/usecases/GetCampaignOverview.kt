package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupConfigStore
import org.hoohoot.homelab.manager.cleanup.domain.ports.DiskSpaceGauge
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity

data class CampaignOverview(
    val campaign: CleanupCampaignEntity?,
    val candidates: List<CleanupCandidateEntity>,
    val diskFreeBytes: Long?,
    val config: CleanupConfig,
)

@ApplicationScoped
class GetCampaignOverview(
    private val configStore: CleanupConfigStore,
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val diskSpaceGauge: DiskSpaceGauge,
) {
    suspend operator fun invoke(): CampaignOverview {
        val config = configStore.effective()
        val campaign = campaigns.findActive()
        return CampaignOverview(
            campaign = campaign,
            candidates = campaign?.let { candidates.listByCampaign(requireNotNull(it.id)) } ?: emptyList(),
            diskFreeBytes = diskSpaceGauge.snapshotFree(config.diskPath),
            config = config,
        )
    }
}
