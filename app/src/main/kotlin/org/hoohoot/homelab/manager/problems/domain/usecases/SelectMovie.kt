package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
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
    suspend operator fun invoke(id: UUID, accessor: Accessor, radarrMovieId: Int): ProblemResult {
        val workflow = workflows.find(id, accessor) ?: return ProblemResult.NotFound
        if (workflow.mediaType != ProblemWorkflowEntity.MEDIA_TYPE_MOVIE) {
            return ProblemResult.Conflict("workflow is not about a movie")
        }
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS) {
            return ProblemResult.Conflict("workflow is not in progress")
        }
        val movie = movieLibrary.allMovies().firstOrNull { it.radarrMovieId == radarrMovieId }
            ?: return ProblemResult.Invalid("movie $radarrMovieId not found in Radarr library")

        val updated = workflows.update(id, accessor) { entity ->
            entity.radarrMovieId = radarrMovieId
            entity.mediaTitle = movie.title
            entity.problemType = null
            entity.state = entity.state.copy(media = movie.toSnapshot(), grabbedRelease = null, description = null)
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
        desiredResolution = desiredResolution,
    )
}
