package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import java.util.UUID

// Admin uniquement : la resource user n'expose pas de suppression
@ApplicationScoped
class DeleteWorkflow(private val workflows: ProblemWorkflows) {
    suspend operator fun invoke(id: UUID): Boolean = workflows.delete(id)
}
