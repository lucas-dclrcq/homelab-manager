package org.hoohoot.homelab.manager.cleanup.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.SuggestResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupConfigStore
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.Suggestions
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import java.time.LocalDateTime
import java.util.UUID

data class SuggestionRequest(
    val mediaKind: String,
    val radarrMovieId: Int?,
    val sonarrSeriesId: Int?,
    val seasonNumber: Int?,
)

// Suggestion de suppression : annoncée sur Matrix, supprimée à l'échéance sauf veto ❌ en réaction
@ApplicationScoped
class SuggestDeletion(
    private val configStore: CleanupConfigStore,
    private val suggestions: Suggestions,
    private val protections: Protections,
    private val movieCatalog: MovieCatalog,
    private val seriesCatalog: SeriesCatalog,
    private val notifier: CleanupNotifier,
) {
    suspend operator fun invoke(username: String, request: SuggestionRequest): SuggestResult {
        if (request.mediaKind == CleanupSuggestionEntity.KIND_SEASON && request.seasonNumber == null) {
            return SuggestResult.Invalid("numéro de saison requis pour proposer une saison")
        }

        val suggestion = when (request.mediaKind) {
            CleanupSuggestionEntity.KIND_MOVIE -> movieSuggestion(request)
            CleanupSuggestionEntity.KIND_SERIES, CleanupSuggestionEntity.KIND_SEASON -> seriesSuggestion(request)
            else -> return SuggestResult.Invalid("type de média inconnu : ${request.mediaKind}")
        } ?: return SuggestResult.MediaNotFound

        val protection = protections.all().firstOrNull {
            it.blocksDeletionOf(suggestion.mediaKind, suggestion.radarrMovieId, suggestion.sonarrSeriesId, suggestion.seasonNumber)
        }
        if (protection != null) return SuggestResult.ProtectedMedia(protection)

        val existing = suggestions.listPending().firstOrNull {
            it.overlaps(suggestion.mediaKind, suggestion.radarrMovieId, suggestion.sonarrSeriesId, suggestion.seasonNumber)
        }
        if (existing != null) return SuggestResult.AlreadySuggested(existing)

        val now = LocalDateTime.now()
        suggestion.suggestedBy = username
        suggestion.status = CleanupSuggestionEntity.STATUS_PENDING
        suggestion.deleteAfter = now.plusDays(configStore.effective().suggestionGraceDays)
        suggestion.createdAt = now
        suggestion.updatedAt = now
        val saved = suggestions.save(suggestion)

        // Annonce best-effort : sans event id, l'exécution n'osera pas supprimer (aucun veto possible)
        try {
            val eventId = notifier.announceSuggestion(saved)
            if (eventId != null) suggestions.update(requireNotNull(saved.id)) { it.announcementEventId = eventId }
        } catch (exception: Exception) {
            Log.error("Cleanup: suggestion announcement failed", exception)
        }

        return SuggestResult.Ok(suggestions.find(requireNotNull(saved.id)) ?: saved)
    }

    private suspend fun movieSuggestion(request: SuggestionRequest): CleanupSuggestionEntity? {
        val movieId = request.radarrMovieId ?: return null
        val movie = movieCatalog.allMovies().firstOrNull { it.radarrMovieId == movieId } ?: return null
        return CleanupSuggestionEntity().apply {
            id = UUID.randomUUID()
            mediaKind = CleanupSuggestionEntity.KIND_MOVIE
            radarrMovieId = movie.radarrMovieId
            title = movie.title
            year = movie.year
            posterUrl = movie.posterUrl
            sizeBytes = movie.sizeBytes
        }
    }

    private suspend fun seriesSuggestion(request: SuggestionRequest): CleanupSuggestionEntity? {
        val seriesId = request.sonarrSeriesId ?: return null
        val series = seriesCatalog.allSeries().firstOrNull { it.sonarrSeriesId == seriesId } ?: return null
        val season = if (request.mediaKind == CleanupSuggestionEntity.KIND_SEASON) {
            series.seasons.firstOrNull { it.seasonNumber == request.seasonNumber } ?: return null
        } else {
            null
        }
        return CleanupSuggestionEntity().apply {
            id = UUID.randomUUID()
            mediaKind = request.mediaKind
            sonarrSeriesId = series.sonarrSeriesId
            seasonNumber = season?.seasonNumber
            title = series.title
            year = series.year
            posterUrl = series.posterUrl
            sizeBytes = season?.sizeBytes ?: series.seasons.sumOf { it.sizeBytes }
        }
    }
}
