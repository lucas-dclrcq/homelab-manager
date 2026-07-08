package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.AnnotatedRelease
import org.hoohoot.homelab.manager.corrector.domain.Release
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.domain.ports.Releases
import java.util.UUID

sealed interface ListReleasesResult {
    data class Ok(val releases: List<AnnotatedRelease>) : ListReleasesResult
    data object NotFound : ListReleasesResult
    data class Conflict(val message: String) : ListReleasesResult
}

@ApplicationScoped
class ListReleases(
    private val workflows: CorrectorWorkflows,
    private val releases: Releases,
) {
    companion object {
        // VOSTFR volontairement exclu : sous-titres, pas doublage
        private val VF_REGEX = Regex("""\b(MULTI|VF[FIQ2]?|FRENCH|TRUEFRENCH)\b""", RegexOption.IGNORE_CASE)
    }

    suspend operator fun invoke(id: UUID, username: String): ListReleasesResult {
        val workflow = workflows.findForUser(id, username) ?: return ListReleasesResult.NotFound
        val movieId = workflow.radarrMovieId
            ?: return ListReleasesResult.Conflict("a movie must be selected first")
        if (workflow.problemType == null) {
            return ListReleasesResult.Conflict("a problem must be selected first")
        }
        val annotated = releases.searchForMovie(movieId)
            .map { AnnotatedRelease(it, it.isFrench()) }
            .sortedWith(compareByDescending<AnnotatedRelease> { it.isFrench }.thenByDescending { it.release.seeders ?: 0 })
        return ListReleasesResult.Ok(annotated)
    }

    private fun Release.isFrench(): Boolean =
        languages.any { it.equals("French", ignoreCase = true) } || VF_REGEX.containsMatchIn(title)
}
