package org.hoohoot.homelab.manager.problems.domain.ports

import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

interface ProblemWorkflows {
    suspend fun listForUser(username: String): List<ProblemWorkflowEntity>
    suspend fun listAll(): List<ProblemWorkflowEntity>
    suspend fun find(id: UUID, accessor: Accessor): ProblemWorkflowEntity?
    suspend fun save(entity: ProblemWorkflowEntity): ProblemWorkflowEntity
    suspend fun update(id: UUID, accessor: Accessor, mutate: (ProblemWorkflowEntity) -> Unit): ProblemWorkflowEntity?
    suspend fun delete(id: UUID): Boolean
    suspend fun completeAwaitingForMovies(movieIdToImportedAt: Map<Int, LocalDateTime>): Int
    suspend fun listAwaitingImport(): List<ProblemWorkflowEntity>
    suspend fun markImportForced(radarrMovieIds: Set<Int>): Int
}
