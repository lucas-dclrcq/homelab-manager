package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CleanupCampaignRepository : Campaigns {

    override suspend fun findActive(): CleanupCampaignEntity? =
        Panache.withSession {
            CleanupCampaignEntity
                .find("status = ?1", CleanupCampaignEntity.STATUS_ANNOUNCED)
                .firstResult()
        }.awaitSuspending()

    override suspend fun find(id: UUID): CleanupCampaignEntity? =
        Panache.withSession {
            CleanupCampaignEntity.findById(id)
        }.awaitSuspending()

    override suspend fun save(entity: CleanupCampaignEntity): CleanupCampaignEntity =
        Panache.withTransaction {
            entity.persist<CleanupCampaignEntity>()
        }.awaitSuspending()

    override suspend fun update(id: UUID, mutate: (CleanupCampaignEntity) -> Unit): CleanupCampaignEntity? =
        Panache.withTransaction {
            CleanupCampaignEntity.findById(id)
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()

    override suspend fun listDue(now: LocalDateTime): List<CleanupCampaignEntity> =
        Panache.withSession {
            CleanupCampaignEntity
                .list("status = ?1 and graceEndsAt <= ?2", CleanupCampaignEntity.STATUS_ANNOUNCED, now)
        }.awaitSuspending()

    override suspend fun listAll(): List<CleanupCampaignEntity> =
        Panache.withSession {
            CleanupCampaignEntity.listAll(Sort.descending("createdAt"))
        }.awaitSuspending()
}
