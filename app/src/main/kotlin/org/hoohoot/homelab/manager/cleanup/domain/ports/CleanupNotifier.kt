package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.ExecutionSummary
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity

interface CleanupNotifier {
    // Retourne l'event id Matrix de l'annonce, null si l'envoi a échoué (best-effort)
    suspend fun announceCampaign(
        campaign: CleanupCampaignEntity,
        candidates: List<CleanupCandidateEntity>,
    ): String?

    suspend fun announceExecution(campaign: CleanupCampaignEntity, summary: ExecutionSummary)

    suspend fun announceCancellation(campaign: CleanupCampaignEntity)

    // Retourne l'event id Matrix de l'annonce, null si l'envoi a échoué (best-effort)
    suspend fun announceSuggestion(suggestion: CleanupSuggestionEntity): String?

    suspend fun announceSuggestionOutcome(suggestion: CleanupSuggestionEntity)
}

interface SuggestionVetoes {
    // Utilisateurs (localparts) ayant réagi ❌ à l'annonce d'une suggestion
    suspend fun vetoers(announcementEventId: String): List<String>
}
