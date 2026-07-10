package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.util.UUID

@ApplicationScoped
class SelectProblem(private val workflows: ProblemWorkflows) {
    companion object {
        private const val MAX_DESCRIPTION_LENGTH = 2000

        private val PROBLEM_TYPES_BY_MEDIA = mapOf(
            ProblemWorkflowEntity.MEDIA_TYPE_MOVIE to setOf(
                ProblemWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH,
                ProblemWorkflowEntity.PROBLEM_OTHER,
            ),
            // Pas de recherche de releases côté Sonarr : les séries passent par la déclaration libre
            ProblemWorkflowEntity.MEDIA_TYPE_TV to setOf(ProblemWorkflowEntity.PROBLEM_OTHER),
        )
    }

    suspend operator fun invoke(id: UUID, accessor: Accessor, problemType: String?, description: String?): ProblemResult {
        val workflow = workflows.find(id, accessor) ?: return ProblemResult.NotFound
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS) {
            return ProblemResult.Conflict("workflow is not in progress")
        }
        if (workflow.radarrMovieId == null && workflow.sonarrSeriesId == null) {
            return ProblemResult.Conflict("a media must be selected first")
        }
        val allowedTypes = PROBLEM_TYPES_BY_MEDIA[workflow.mediaType].orEmpty()
        if (problemType !in allowedTypes) {
            return ProblemResult.Invalid("problemType must be one of ${allowedTypes.joinToString("', '", "'", "'")}")
        }

        val trimmedDescription = description?.trim()
        if (problemType == ProblemWorkflowEntity.PROBLEM_OTHER) {
            if (trimmedDescription.isNullOrBlank()) {
                return ProblemResult.Invalid("a description is required for problemType 'other'")
            }
            if (trimmedDescription.length > MAX_DESCRIPTION_LENGTH) {
                return ProblemResult.Invalid("description must not exceed $MAX_DESCRIPTION_LENGTH characters")
            }
        }

        val updated = workflows.update(id, accessor) { entity ->
            entity.problemType = problemType
            if (problemType == ProblemWorkflowEntity.PROBLEM_OTHER) {
                entity.status = ProblemWorkflowEntity.STATUS_REPORTED
                entity.state = entity.state.copy(description = trimmedDescription)
            }
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }
}
