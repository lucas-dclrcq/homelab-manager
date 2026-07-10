package org.hoohoot.homelab.manager.problems.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
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

    override suspend fun findForUser(id: UUID, username: String): ProblemWorkflowEntity? =
        Panache.withSession {
            ProblemWorkflowEntity.find("id = ?1 and username = ?2", id, username).firstResult()
        }.awaitSuspending()

    override suspend fun save(entity: ProblemWorkflowEntity): ProblemWorkflowEntity =
        Panache.withTransaction {
            entity.persist<ProblemWorkflowEntity>()
        }.awaitSuspending()

    override suspend fun update(id: UUID, username: String, mutate: (ProblemWorkflowEntity) -> Unit): ProblemWorkflowEntity? =
        Panache.withTransaction {
            ProblemWorkflowEntity.find("id = ?1 and username = ?2", id, username).firstResult()
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()

    // Complète les workflows en attente dont le film vient d'être importé après le grab. Idempotent.
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
