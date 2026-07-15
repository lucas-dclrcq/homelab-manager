package org.hoohoot.homelab.manager.problems.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class ProblemWorkflowRepository : ProblemWorkflows {

    override suspend fun listForUser(username: String): List<ProblemWorkflowEntity> =
        Panache.withSession {
            ProblemWorkflowEntity
                .list("username = ?1", Sort.descending("updatedAt"), username)
        }.awaitSuspending()

    override suspend fun listAll(): List<ProblemWorkflowEntity> =
        Panache.withSession {
            ProblemWorkflowEntity.listAll(Sort.descending("updatedAt"))
        }.awaitSuspending()

    override suspend fun find(id: UUID, accessor: Accessor): ProblemWorkflowEntity? =
        Panache.withSession { findQuery(id, accessor) }.awaitSuspending()

    override suspend fun save(entity: ProblemWorkflowEntity): ProblemWorkflowEntity =
        Panache.withTransaction {
            entity.persist<ProblemWorkflowEntity>()
        }.awaitSuspending()

    override suspend fun update(id: UUID, accessor: Accessor, mutate: (ProblemWorkflowEntity) -> Unit): ProblemWorkflowEntity? =
        Panache.withTransaction {
            findQuery(id, accessor)
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()

    override suspend fun delete(id: UUID): Boolean =
        Panache.withTransaction {
            ProblemWorkflowEntity.deleteById(id)
        }.awaitSuspending()

    private fun findQuery(id: UUID, accessor: Accessor) = when (accessor) {
        is Accessor.User -> ProblemWorkflowEntity.find("id = ?1 and username = ?2", id, accessor.username).firstResult()
        is Accessor.Admin -> ProblemWorkflowEntity.find("id = ?1", id).firstResult()
    }

    override suspend fun completeAwaitingForMovies(movieIdToImportedAt: Map<Int, LocalDateTime>): Int {
        if (movieIdToImportedAt.isEmpty()) return 0
        return Panache.withTransaction {
            ProblemWorkflowEntity
                .list(
                    "status = ?1 and radarrMovieId in ?2",
                    ProblemWorkflowEntity.STATUS_AWAITING_IMPORT,
                    movieIdToImportedAt.keys,
                )
                .invoke { workflows ->
                    workflows.forEach { workflow ->
                        val importedAt = movieIdToImportedAt[workflow.radarrMovieId] ?: return@forEach
                        val grabbedAt = workflow.grabbedAt
                        if (grabbedAt != null && importedAt.isAfter(grabbedAt)) {
                            workflow.status = ProblemWorkflowEntity.STATUS_COMPLETED
                            workflow.completedAt = LocalDateTime.now()
                            workflow.updatedAt = LocalDateTime.now()
                        }
                    }
                }
        }.awaitSuspending().count { it.status == ProblemWorkflowEntity.STATUS_COMPLETED }
    }
}
