package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CleanupCandidateRepository : Candidates {

    override suspend fun saveAll(entities: List<CleanupCandidateEntity>) {
        // persist(emptyList) crash "The Uni set is empty"
        if (entities.isEmpty()) return
        Panache.withTransaction {
            CleanupCandidateEntity.persist(entities)
        }.awaitSuspending()
    }

    override suspend fun listByCampaign(campaignId: UUID): List<CleanupCandidateEntity> =
        Panache.withSession {
            CleanupCandidateEntity
                .list("campaignId = ?1", Sort.descending("score"), campaignId)
        }.awaitSuspending()

    override suspend fun find(id: UUID): CleanupCandidateEntity? =
        Panache.withSession {
            CleanupCandidateEntity.findById(id)
        }.awaitSuspending()

    override suspend fun update(id: UUID, mutate: (CleanupCandidateEntity) -> Unit): CleanupCandidateEntity? =
        Panache.withTransaction {
            CleanupCandidateEntity.findById(id)
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()
}
