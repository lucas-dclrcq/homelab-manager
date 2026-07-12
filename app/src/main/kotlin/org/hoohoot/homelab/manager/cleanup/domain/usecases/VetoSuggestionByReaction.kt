package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.domain.ports.Suggestions
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import java.time.LocalDateTime

// Veto immédiat quand une réaction ❌ arrive sur l'annonce : retour direct dans le chat sans
// attendre l'échéance. La lecture des réactions à l'échéance reste le filet de sécurité
// (réactions posées pendant un redémarrage de l'app).
@ApplicationScoped
class VetoSuggestionByReaction(
    private val suggestions: Suggestions,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(announcementEventId: String, username: String): CleanupSuggestionEntity? {
        val suggestion = suggestions.findPendingByAnnouncementEvent(announcementEventId) ?: return null

        val updated = suggestions.update(requireNotNull(suggestion.id)) {
            it.status = CleanupSuggestionEntity.STATUS_VETOED
            it.vetoedBy = username
            it.vetoedAt = LocalDateTime.now()
        } ?: return null

        try {
            notifier.announceSuggestionOutcome(updated)
        } catch (exception: Exception) {
            Log.error("Cleanup: suggestion veto acknowledgement failed", exception)
        }

        return updated
    }
}
