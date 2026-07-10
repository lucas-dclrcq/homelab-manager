package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.LibraryMovie
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.ports.MovieLibrary
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.problems.infra.MediaSnapshot
import java.util.UUID

@ApplicationScoped
class SelectMovie(
    private val workflows: ProblemWorkflows,
    private val movieLibrary: MovieLibrary,
) {
    suspend operator fun invoke(id: UUID, username: String, radarrMovieId: Int): ProblemResult {
        val workflow = workflows.findForUser(id, username) ?: return ProblemResult.NotFound
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS) {
            return ProblemResult.Conflict("workflow is not in progress")
        }
        val movie = movieLibrary.allMovies().firstOrNull { it.radarrMovieId == radarrMovieId }
            ?: return ProblemResult.Invalid("movie $radarrMovieId not found in Radarr library")

        val updated = workflows.update(id, username) { entity ->
            entity.radarrMovieId = radarrMovieId
            entity.mediaTitle = movie.title
            entity.problemType = null
            entity.state = entity.state.copy(media = movie.toSnapshot(), grabbedRelease = null)
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }

    private fun LibraryMovie.toSnapshot() = MediaSnapshot(
        title = title,
        year = year,
        posterUrl = posterUrl,
        overview = overview,
        currentQuality = currentQuality,
        currentLanguages = currentLanguages,
    )
}
