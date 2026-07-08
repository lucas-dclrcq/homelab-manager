package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.CorrectorResult
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import java.util.UUID

@ApplicationScoped
class SelectProblem(private val workflows: CorrectorWorkflows) {
    suspend operator fun invoke(id: UUID, username: String, problemType: String?): CorrectorResult {
        if (problemType != CorrectorWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH) {
            return CorrectorResult.Invalid("problemType must be '${CorrectorWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH}'")
        }
        val workflow = workflows.findForUser(id, username) ?: return CorrectorResult.NotFound
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS) {
            return CorrectorResult.Conflict("workflow is not in progress")
        }
        if (workflow.radarrMovieId == null) {
            return CorrectorResult.Conflict("a movie must be selected first")
        }
        val updated = workflows.update(id, username) { entity ->
            entity.problemType = problemType
        } ?: return CorrectorResult.NotFound
        return CorrectorResult.Ok(updated)
    }
}
