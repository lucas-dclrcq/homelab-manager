package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.CleanupMovie
import org.hoohoot.homelab.manager.cleanup.domain.CleanupSeries
import org.hoohoot.homelab.manager.cleanup.domain.Titles
import org.hoohoot.homelab.manager.cleanup.domain.ports.MovieCatalog
import org.hoohoot.homelab.manager.cleanup.domain.ports.SeriesCatalog

@ApplicationScoped
class SearchProtectableMedia(
    private val movieCatalog: MovieCatalog,
    private val seriesCatalog: SeriesCatalog,
) {
    companion object {
        private const val MAX_SEARCH_RESULTS = 10
    }

    suspend fun movies(query: String): List<CleanupMovie> =
        search(query, { movieCatalog.allMovies() }, { it.title })

    suspend fun series(query: String): List<CleanupSeries> =
        search(query, { seriesCatalog.allSeries() }, { it.title })

    private suspend fun <T> search(query: String, all: suspend () -> List<T>, title: (T) -> String): List<T> {
        val normalizedQuery = Titles.normalize(query)
        if (normalizedQuery.isBlank()) return emptyList()
        return all()
            .filter { Titles.normalize(title(it)).contains(normalizedQuery) }
            .take(MAX_SEARCH_RESULTS)
    }
}
