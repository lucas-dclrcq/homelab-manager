package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.util.UUID

@ApplicationScoped
class AbandonWorkflow(private val workflows: ProblemWorkflows) {
    suspend operator fun invoke(id: UUID, username: String): ProblemResult {
        val workflow = workflows.findForUser(id, username) ?: return ProblemResult.NotFound
        if (workflow.status == ProblemWorkflowEntity.STATUS_COMPLETED) {
            return ProblemResult.Conflict("workflow is already completed")
        }
        val updated = workflows.update(id, username) { entity ->
            entity.status = ProblemWorkflowEntity.STATUS_ABANDONED
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }
}
