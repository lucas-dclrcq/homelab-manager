package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.LibraryMovie
import org.hoohoot.homelab.manager.problems.domain.LibrarySeries
import org.hoohoot.homelab.manager.problems.domain.ports.MovieLibrary
import org.hoohoot.homelab.manager.problems.domain.ports.SeriesLibrary
import java.text.Normalizer

@ApplicationScoped
class SearchLibrary(
    private val movieLibrary: MovieLibrary,
    private val seriesLibrary: SeriesLibrary,
) {
    companion object {
        private const val MAX_SEARCH_RESULTS = 10
    }

    // Les APIs *arr ne supportent pas de recherche : on filtre la bibliothèque côté backend
    suspend fun movies(query: String): List<LibraryMovie> =
        search(query, { movieLibrary.allMovies() }, { it.title })

    suspend fun series(query: String): List<LibrarySeries> =
        search(query, { seriesLibrary.allSeries() }, { it.title })

    private suspend fun <T> search(query: String, all: suspend () -> List<T>, title: (T) -> String): List<T> {
        val normalizedQuery = query.normalized()
        if (normalizedQuery.isBlank()) return emptyList()
        return all()
            .filter { title(it).normalized().contains(normalizedQuery) }
            .take(MAX_SEARCH_RESULTS)
    }

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
}
