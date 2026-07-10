package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.AnnotatedRelease
import org.hoohoot.homelab.manager.problems.domain.Release
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.ports.Releases
import java.util.UUID

sealed interface ListReleasesResult {
    data class Ok(val releases: List<AnnotatedRelease>) : ListReleasesResult
    data object NotFound : ListReleasesResult
    data class Conflict(val message: String) : ListReleasesResult
}

@ApplicationScoped
class ListReleases(
    private val workflows: ProblemWorkflows,
    private val releases: Releases,
) {
    companion object {
        // VOSTFR volontairement exclu : sous-titres, pas doublage
        private val VF_REGEX = Regex("""\b(MULTI|VF[FIQ2]?|FRENCH|TRUEFRENCH)\b""", RegexOption.IGNORE_CASE)
        private val RESOLUTION_REGEX = Regex("""(2160|1080|720|576|480)p""", RegexOption.IGNORE_CASE)
        // Le 1080p est toujours considéré comme un bon upgrade, quel que soit le profil
        private const val HD_RESOLUTION = 1080

        private fun resolutionOf(quality: String?): Int? =
            quality?.let { RESOLUTION_REGEX.find(it)?.groupValues?.get(1)?.toIntOrNull() }
    }

    suspend operator fun invoke(id: UUID, accessor: Accessor): ListReleasesResult {
        val workflow = workflows.find(id, accessor) ?: return ListReleasesResult.NotFound
        val movieId = workflow.radarrMovieId
            ?: return ListReleasesResult.Conflict("a movie must be selected first")
        if (workflow.problemType == null) {
            return ListReleasesResult.Conflict("a problem must be selected first")
        }
        // Les règles Radarr rejettent la plupart des releases FR : on recommande nous-mêmes celles
        // qui sont en torrent, à une bonne résolution, avec un tag VF/MULTI dans le titre.
        val desiredResolution = workflow.state.media?.desiredResolution?.toIntOrNull()
        val annotated = releases.searchForMovie(movieId)
            .map { AnnotatedRelease(it, it.isFrench(), it.isRecommended(desiredResolution)) }
            .sortedWith(
                compareByDescending<AnnotatedRelease> { it.isRecommended }
                    .thenByDescending { it.isFrench }
                    .thenByDescending { it.release.seeders ?: 0 },
            )
        return ListReleasesResult.Ok(annotated)
    }

    private fun Release.isFrench(): Boolean =
        languages.any { it.equals("French", ignoreCase = true) } || VF_REGEX.containsMatchIn(title)

    // On recommande les torrents VF/MULTI en 1080p ou mieux. Si le profil Radarr vise plus bas
    // (ex : 720p) on descend jusqu'à sa cible ; le plancher ne dépasse jamais 1080p pour ne jamais
    // écarter un upgrade HD, même quand le profil demande de la 4K. Profil inconnu → permissif.
    private fun Release.isRecommended(desiredResolution: Int?): Boolean {
        if (!protocol.equals("torrent", ignoreCase = true)) return false
        if (!VF_REGEX.containsMatchIn(title)) return false
        if (desiredResolution == null) return true
        val releaseResolution = resolutionOf(quality) ?: return false
        val floor = minOf(desiredResolution, HD_RESOLUTION)
        return releaseResolution >= floor
    }
}
