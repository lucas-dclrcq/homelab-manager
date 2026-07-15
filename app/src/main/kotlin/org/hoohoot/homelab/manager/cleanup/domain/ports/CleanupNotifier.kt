package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.ExecutionSummary
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity

interface CleanupNotifier {
    suspend fun announceCampaign(
        campaign: CleanupCampaignEntity,
        candidates: List<CleanupCandidateEntity>,
    ): String?

    suspend fun announceExecution(campaign: CleanupCampaignEntity, summary: ExecutionSummary)

    suspend fun announceCancellation(campaign: CleanupCampaignEntity)

    suspend fun announceSuggestion(suggestion: CleanupSuggestionEntity): String?

    suspend fun announceSuggestionOutcome(suggestion: CleanupSuggestionEntity)
}

interface SuggestionVetoes {
    suspend fun vetoers(announcementEventId: String): List<String>
}
