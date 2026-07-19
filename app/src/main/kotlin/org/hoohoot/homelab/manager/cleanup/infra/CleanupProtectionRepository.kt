package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Page
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.ports.ProtectionsPage
import java.util.UUID

@ApplicationScoped
class CleanupProtectionRepository : Protections {

    override suspend fun all(): List<CleanupProtectionEntity> =
        Panache.withSession {
            CleanupProtectionEntity.listAll(Sort.descending("createdAt"))
        }.awaitSuspending()

    override suspend fun page(page: Int, pageSize: Int): ProtectionsPage =
        Panache.withSession {
            val query = CleanupProtectionEntity
                .findAll(Sort.descending("createdAt"))
                .page(Page.of(page, pageSize))
            query.count().flatMap { total ->
                query.list().map { items -> ProtectionsPage(items, total, page, pageSize) }
            }
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
