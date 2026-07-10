package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.LibrarySeries
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.ports.SeriesLibrary
import org.hoohoot.homelab.manager.problems.infra.MediaSnapshot
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.util.UUID

@ApplicationScoped
class SelectSeries(
    private val workflows: ProblemWorkflows,
    private val seriesLibrary: SeriesLibrary,
) {
    suspend operator fun invoke(id: UUID, accessor: Accessor, sonarrSeriesId: Int): ProblemResult {
        val workflow = workflows.find(id, accessor) ?: return ProblemResult.NotFound
        if (workflow.mediaType != ProblemWorkflowEntity.MEDIA_TYPE_TV) {
            return ProblemResult.Conflict("workflow is not about a series")
        }
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS) {
            return ProblemResult.Conflict("workflow is not in progress")
        }
        val series = seriesLibrary.allSeries().firstOrNull { it.sonarrSeriesId == sonarrSeriesId }
            ?: return ProblemResult.Invalid("series $sonarrSeriesId not found in Sonarr library")

        val updated = workflows.update(id, accessor) { entity ->
            entity.sonarrSeriesId = sonarrSeriesId
            entity.mediaTitle = series.title
            entity.problemType = null
            entity.state = entity.state.copy(media = series.toSnapshot(), grabbedRelease = null, description = null)
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }

    private fun LibrarySeries.toSnapshot() = MediaSnapshot(
        title = title,
        year = year,
        posterUrl = posterUrl,
        overview = overview,
    )
}
