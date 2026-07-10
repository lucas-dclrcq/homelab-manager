package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ResolveWorkflow(private val workflows: ProblemWorkflows) {
    companion object {
        private val RESOLVABLE_STATUSES = setOf(
            ProblemWorkflowEntity.STATUS_REPORTED,
            ProblemWorkflowEntity.STATUS_IN_PROGRESS,
            ProblemWorkflowEntity.STATUS_AWAITING_IMPORT,
        )
    }

    suspend operator fun invoke(id: UUID, accessor: Accessor): ProblemResult {
        val workflow = workflows.find(id, accessor) ?: return ProblemResult.NotFound
        if (workflow.status !in RESOLVABLE_STATUSES) {
            return ProblemResult.Conflict("workflow is already closed")
        }
        val updated = workflows.update(id, accessor) { entity ->
            entity.status = ProblemWorkflowEntity.STATUS_RESOLVED
            entity.completedAt = LocalDateTime.now()
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }
}
