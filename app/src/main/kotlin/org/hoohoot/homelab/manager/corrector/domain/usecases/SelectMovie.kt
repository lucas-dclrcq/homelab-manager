package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.CorrectorResult
import org.hoohoot.homelab.manager.corrector.domain.LibraryMovie
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.domain.ports.MovieLibrary
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import org.hoohoot.homelab.manager.corrector.infra.MovieSnapshot
import java.util.UUID

@ApplicationScoped
class SelectMovie(
    private val workflows: CorrectorWorkflows,
    private val movieLibrary: MovieLibrary,
) {
    suspend operator fun invoke(id: UUID, username: String, radarrMovieId: Int): CorrectorResult {
        val workflow = workflows.findForUser(id, username) ?: return CorrectorResult.NotFound
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS) {
            return CorrectorResult.Conflict("workflow is not in progress")
        }
        val movie = movieLibrary.allMovies().firstOrNull { it.radarrMovieId == radarrMovieId }
            ?: return CorrectorResult.Invalid("movie $radarrMovieId not found in Radarr library")

        val updated = workflows.update(id, username) { entity ->
            entity.radarrMovieId = radarrMovieId
            entity.movieTitle = movie.title
            entity.problemType = null
            entity.state = entity.state.copy(movie = movie.toSnapshot(), grabbedRelease = null)
        } ?: return CorrectorResult.NotFound
        return CorrectorResult.Ok(updated)
    }

    private fun LibraryMovie.toSnapshot() = MovieSnapshot(
        title = title,
        year = year,
        posterUrl = posterUrl,
        overview = overview,
        currentQuality = currentQuality,
        currentLanguages = currentLanguages,
    )
}
