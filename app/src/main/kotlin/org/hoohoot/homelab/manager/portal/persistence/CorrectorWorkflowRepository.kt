package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CorrectorWorkflowRepository {

    suspend fun listForUser(username: String): List<CorrectorWorkflowEntity> =
        Panache.withSession {
            CorrectorWorkflowEntity
                .list("username = ?1", Sort.descending("updatedAt"), username)
        }.awaitSuspending()

    suspend fun findForUser(id: UUID, username: String): CorrectorWorkflowEntity? =
        Panache.withSession {
            CorrectorWorkflowEntity.find("id = ?1 and username = ?2", id, username).firstResult()
        }.awaitSuspending()

    suspend fun save(entity: CorrectorWorkflowEntity): CorrectorWorkflowEntity =
        Panache.withTransaction {
            entity.persist<CorrectorWorkflowEntity>()
        }.awaitSuspending()

    suspend fun update(id: UUID, username: String, mutate: (CorrectorWorkflowEntity) -> Unit): CorrectorWorkflowEntity? =
        Panache.withTransaction {
            CorrectorWorkflowEntity.find("id = ?1 and username = ?2", id, username).firstResult()
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()

    // Complète les workflows en attente dont le film vient d'être importé après le grab. Idempotent.
    suspend fun completeAwaitingForMovies(movieIdToImportedAt: Map<Int, LocalDateTime>): Int {
        if (movieIdToImportedAt.isEmpty()) return 0
        return Panache.withTransaction {
            CorrectorWorkflowEntity
                .list(
                    "status = ?1 and radarrMovieId in ?2",
                    CorrectorWorkflowEntity.STATUS_AWAITING_IMPORT,
                    movieIdToImportedAt.keys,
                )
                .invoke { workflows ->
                    workflows.forEach { workflow ->
                        val importedAt = movieIdToImportedAt[workflow.radarrMovieId] ?: return@forEach
                        val grabbedAt = workflow.grabbedAt
                        if (grabbedAt != null && importedAt.isAfter(grabbedAt)) {
                            workflow.status = CorrectorWorkflowEntity.STATUS_COMPLETED
                            workflow.completedAt = LocalDateTime.now()
                            workflow.updatedAt = LocalDateTime.now()
                        }
                    }
                }
        }.awaitSuspending().count { it.status == CorrectorWorkflowEntity.STATUS_COMPLETED }
    }
}
