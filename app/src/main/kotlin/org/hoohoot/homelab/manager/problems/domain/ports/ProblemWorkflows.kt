package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

interface ProblemWorkflows {
    suspend fun listForUser(username: String): List<ProblemWorkflowEntity>
    suspend fun findForUser(id: UUID, username: String): ProblemWorkflowEntity?
    suspend fun save(entity: ProblemWorkflowEntity): ProblemWorkflowEntity
    suspend fun update(id: UUID, username: String, mutate: (ProblemWorkflowEntity) -> Unit): ProblemWorkflowEntity?
    suspend fun completeAwaitingForMovies(movieIdToImportedAt: Map<Int, LocalDateTime>): Int
}
