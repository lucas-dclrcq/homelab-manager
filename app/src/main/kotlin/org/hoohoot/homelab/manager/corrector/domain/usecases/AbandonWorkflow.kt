package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.CorrectorResult
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import java.util.UUID

@ApplicationScoped
class AbandonWorkflow(private val workflows: CorrectorWorkflows) {
    suspend operator fun invoke(id: UUID, username: String): CorrectorResult {
        val workflow = workflows.findForUser(id, username) ?: return CorrectorResult.NotFound
        if (workflow.status == CorrectorWorkflowEntity.STATUS_COMPLETED) {
            return CorrectorResult.Conflict("workflow is already completed")
        }
        val updated = workflows.update(id, username) { entity ->
            entity.status = CorrectorWorkflowEntity.STATUS_ABANDONED
        } ?: return CorrectorResult.NotFound
        return CorrectorResult.Ok(updated)
    }
}
