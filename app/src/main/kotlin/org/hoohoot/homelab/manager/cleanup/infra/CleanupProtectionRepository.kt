package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import java.util.UUID

@ApplicationScoped
class CleanupProtectionRepository : Protections {

    override suspend fun all(): List<CleanupProtectionEntity> =
        Panache.withSession {
            CleanupProtectionEntity.listAll(Sort.descending("createdAt"))
        }.awaitSuspending()

    override suspend fun find(id: UUID): CleanupProtectionEntity? =
        Panache.withSession {
            CleanupProtectionEntity.findById(id)
        }.awaitSuspending()

    override suspend fun save(entity: CleanupProtectionEntity): CleanupProtectionEntity =
        Panache.withTransaction {
            entity.persist<CleanupProtectionEntity>()
        }.awaitSuspending()

    override suspend fun delete(id: UUID): Boolean =
        Panache.withTransaction {
            CleanupProtectionEntity.deleteById(id)
        }.awaitSuspending()
}
