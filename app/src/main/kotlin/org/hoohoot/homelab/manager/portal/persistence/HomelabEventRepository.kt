package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Page
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped

data class HomelabEventPage(
    val items: List<HomelabEventEntity>,
    val totalCount: Long,
)

@ApplicationScoped
class HomelabEventRepository {

    suspend fun save(entity: HomelabEventEntity) {
        Panache.withTransaction {
            entity.persist<HomelabEventEntity>()
        }.awaitSuspending()
    }

    suspend fun findPage(page: Int, pageSize: Int): HomelabEventPage {
        val items = Panache.withSession {
            HomelabEventEntity.findAll(Sort.descending("occurredAt", "id"))
                .page(Page.of(page, pageSize))
                .list()
        }.awaitSuspending()
        val totalCount = Panache.withSession {
            HomelabEventEntity.count()
        }.awaitSuspending()
        return HomelabEventPage(items, totalCount)
    }
}
