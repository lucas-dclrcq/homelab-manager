package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.LibraryMovie
import org.hoohoot.homelab.manager.corrector.domain.ports.MovieLibrary
import java.text.Normalizer

@ApplicationScoped
class SearchLibrary(private val movieLibrary: MovieLibrary) {
    companion object {
        private const val MAX_SEARCH_RESULTS = 10
    }

    // GET /api/v3/movie ne supporte pas de recherche : on filtre la bibliothèque côté backend
    suspend operator fun invoke(query: String): List<LibraryMovie> {
        val normalizedQuery = query.normalized()
        if (normalizedQuery.isBlank()) return emptyList()
        return movieLibrary.allMovies()
            .filter { it.title.normalized().contains(normalizedQuery) }
            .take(MAX_SEARCH_RESULTS)
    }

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
}
