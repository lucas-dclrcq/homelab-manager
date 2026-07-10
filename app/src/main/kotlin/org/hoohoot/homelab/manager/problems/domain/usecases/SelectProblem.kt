package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.util.UUID

@ApplicationScoped
class SelectProblem(private val workflows: ProblemWorkflows) {
    suspend operator fun invoke(id: UUID, username: String, problemType: String?): ProblemResult {
        if (problemType != ProblemWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH) {
            return ProblemResult.Invalid("problemType must be '${ProblemWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH}'")
        }
        val workflow = workflows.findForUser(id, username) ?: return ProblemResult.NotFound
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS) {
            return ProblemResult.Conflict("workflow is not in progress")
        }
        if (workflow.radarrMovieId == null) {
            return ProblemResult.Conflict("a movie must be selected first")
        }
        val updated = workflows.update(id, username) { entity ->
            entity.problemType = problemType
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }
}
