package org.hoohoot.homelab.manager.portal.corrector

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrGrabRequest
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrRelease
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrRestClient
import java.text.Normalizer

data class AnnotatedRelease(
    val release: RadarrRelease,
    val isFrench: Boolean,
)

@ApplicationScoped
class CorrectorService(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
) {
    companion object {
        private const val MAX_SEARCH_RESULTS = 10

        // VOSTFR volontairement exclu : sous-titres, pas doublage
        private val VF_REGEX = Regex("""\b(MULTI|VF[FIQ2]?|FRENCH|TRUEFRENCH)\b""", RegexOption.IGNORE_CASE)
    }

    // GET /api/v3/movie ne supporte pas de recherche : on filtre la bibliothèque côté backend
    suspend fun searchLibrary(query: String): List<RadarrMovie> {
        val normalizedQuery = query.normalized()
        if (normalizedQuery.isBlank()) return emptyList()
        return radarrRestClient.getMovies().orEmpty()
            .filter { it.title?.normalized()?.contains(normalizedQuery) == true }
            .take(MAX_SEARCH_RESULTS)
    }

    suspend fun findMovie(radarrMovieId: Int): RadarrMovie? =
        radarrRestClient.getMovies().orEmpty().firstOrNull { it.id == radarrMovieId }

    suspend fun searchReleases(radarrMovieId: Int): List<AnnotatedRelease> =
        radarrRestClient.searchReleases(radarrMovieId).orEmpty()
            .map { AnnotatedRelease(it, it.isFrench()) }
            .sortedWith(compareByDescending<AnnotatedRelease> { it.isFrench }.thenByDescending { it.release.seeders ?: 0 })

    suspend fun grabRelease(guid: String, indexerId: Int) {
        radarrRestClient.grabRelease(RadarrGrabRequest(guid, indexerId))
    }

    private fun RadarrRelease.isFrench(): Boolean =
        languages.any { it.name.equals("French", ignoreCase = true) } ||
            title?.let { VF_REGEX.containsMatchIn(it) } == true

    private fun String.normalized(): String =
        Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
}
