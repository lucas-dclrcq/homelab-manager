package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.DeleteOutcome
import org.hoohoot.homelab.manager.cleanup.domain.ports.ActiveProblems
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeasonEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesEraser
import org.hoohoot.homelab.manager.cleanup.domain.ports.SuggestionVetoes
import org.hoohoot.homelab.manager.cleanup.domain.ports.Suggestions
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ExecuteDueSuggestions(
    private val suggestions: Suggestions,
    private val protections: Protections,
    private val activeProblems: ActiveProblems,
    private val movieEraser: MovieEraser,
    private val seriesEraser: SeriesEraser,
    private val seasonEraser: SeasonEraser,
    private val vetoes: SuggestionVetoes,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(): List<CleanupSuggestionEntity> =
        suggestions.listDue(LocalDateTime.now()).mapNotNull { execute(it) }

    private suspend fun execute(suggestion: CleanupSuggestionEntity): CleanupSuggestionEntity? {
        val suggestionId = requireNotNull(suggestion.id)

        // Sans annonce, personne n'a pu poser de veto : on ne supprime pas
        val announcementEventId = suggestion.announcementEventId
            ?: return finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_SKIPPED
                it.failureReason = "l'annonce Matrix n'a jamais été envoyée, suppression annulée par prudence"
            }

        // Le veto s'exprime en réaction ❌ sur l'annonce : lu à l'échéance, il survit aux redémarrages.
        // Si Matrix est injoignable, on reporte plutôt que de supprimer sans avoir lu les vetos.
        val vetoer = try {
            vetoes.vetoers(announcementEventId).firstOrNull()
        } catch (exception: Exception) {
            Log.error("Cleanup: could not read veto reactions for '${suggestion.title}', postponing", exception)
            return null
        }
        if (vetoer != null) {
            return finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_VETOED
                it.vetoedBy = vetoer
                it.vetoedAt = LocalDateTime.now()
            }
        }

        // Re-check juste avant suppression : une protection ou un problème a pu apparaître
        if (protections.all().any {
                it.blocksDeletionOf(suggestion.mediaKind, suggestion.radarrMovieId, suggestion.sonarrSeriesId, suggestion.seasonNumber)
            }
        ) {
            return finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_SKIPPED
                it.failureReason = "protégé entre-temps"
            }
        }
        val problemIds = activeProblems.activeMediaIds()
        if (suggestion.radarrMovieId in problemIds.radarrMovieIds ||
            suggestion.sonarrSeriesId in problemIds.sonarrSeriesIds
        ) {
            return finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_SKIPPED
                it.failureReason = "un problème est en cours sur ce média"
            }
        }

        return when (val outcome = delete(suggestion)) {
            is DeleteOutcome.Deleted -> finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_DELETED
                it.deletedAt = LocalDateTime.now()
                it.freedBytes = outcome.freedBytes
            }
            DeleteOutcome.AlreadyGone -> finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_DELETED
                it.deletedAt = LocalDateTime.now()
                it.freedBytes = 0
                it.failureReason = "déjà absent de la bibliothèque"
            }
            is DeleteOutcome.Failed -> finish(suggestionId) {
                it.status = CleanupSuggestionEntity.STATUS_FAILED
                it.failureReason = outcome.reason
            }
        }
    }

    // Une erreur sur une suggestion ne doit pas interrompre les suivantes
    private suspend fun delete(suggestion: CleanupSuggestionEntity): DeleteOutcome = try {
        when (suggestion.mediaKind) {
            CleanupSuggestionEntity.KIND_MOVIE ->
                movieEraser.deleteMovie(requireNotNull(suggestion.radarrMovieId), suggestion.sizeBytes)
            CleanupSuggestionEntity.KIND_SERIES ->
                seriesEraser.deleteSeries(requireNotNull(suggestion.sonarrSeriesId), suggestion.sizeBytes)
            CleanupSuggestionEntity.KIND_SEASON ->
                seasonEraser.deleteSeason(requireNotNull(suggestion.sonarrSeriesId), requireNotNull(suggestion.seasonNumber))
            else -> DeleteOutcome.Failed("type de média inconnu : ${suggestion.mediaKind}")
        }
    } catch (exception: Exception) {
        Log.error("Cleanup: suggested deletion failed for '${suggestion.title}'", exception)
        DeleteOutcome.Failed(exception.message ?: exception.javaClass.simpleName)
    }

    private suspend fun finish(
        suggestionId: UUID,
        mutate: (CleanupSuggestionEntity) -> Unit,
    ): CleanupSuggestionEntity? {
        val updated = suggestions.update(suggestionId, mutate) ?: return null
        try {
            notifier.announceSuggestionOutcome(updated)
        } catch (exception: Exception) {
            Log.error("Cleanup: suggestion outcome announcement failed", exception)
        }
        return updated
    }
}
