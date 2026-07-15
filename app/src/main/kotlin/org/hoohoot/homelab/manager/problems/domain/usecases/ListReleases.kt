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
        private val VF_REGEX = Regex("""\b(MULTI|VF[FIQ2]?|FRENCH|TRUEFRENCH)\b""", RegexOption.IGNORE_CASE)
        private val RESOLUTION_REGEX = Regex("""(2160|1080|720|576|480)p""", RegexOption.IGNORE_CASE)

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

    private fun Release.isRecommended(desiredResolution: Int?): Boolean {
        if (!protocol.equals("torrent", ignoreCase = true)) return false
        if (!VF_REGEX.containsMatchIn(title)) return false
        if (desiredResolution == null) return true
        return resolutionOf(quality) == desiredResolution
    }
}
