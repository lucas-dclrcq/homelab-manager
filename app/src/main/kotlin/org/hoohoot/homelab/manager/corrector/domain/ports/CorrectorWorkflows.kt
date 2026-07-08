package org.hoohoot.homelab.manager.corrector.domain.ports

import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

interface CorrectorWorkflows {
    suspend fun listForUser(username: String): List<CorrectorWorkflowEntity>
    suspend fun findForUser(id: UUID, username: String): CorrectorWorkflowEntity?
    suspend fun save(entity: CorrectorWorkflowEntity): CorrectorWorkflowEntity
    suspend fun update(id: UUID, username: String, mutate: (CorrectorWorkflowEntity) -> Unit): CorrectorWorkflowEntity?
    suspend fun completeAwaitingForMovies(movieIdToImportedAt: Map<Int, LocalDateTime>): Int
}
