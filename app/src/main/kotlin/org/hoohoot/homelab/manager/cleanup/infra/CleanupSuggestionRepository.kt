package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ports.Suggestions
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CleanupSuggestionRepository : Suggestions {

    override suspend fun listPending(): List<CleanupSuggestionEntity> =
        Panache.withSession {
            CleanupSuggestionEntity
                .list("status = ?1", Sort.ascending("deleteAfter"), CleanupSuggestionEntity.STATUS_PENDING)
        }.awaitSuspending()

    override suspend fun listRecent(resolvedSince: LocalDateTime): List<CleanupSuggestionEntity> =
        Panache.withSession {
            CleanupSuggestionEntity.list(
                "status = ?1 or updatedAt >= ?2",
                Sort.ascending("deleteAfter"),
                CleanupSuggestionEntity.STATUS_PENDING,
                resolvedSince,
            )
        }.awaitSuspending()

    override suspend fun listDue(now: LocalDateTime): List<CleanupSuggestionEntity> =
        Panache.withSession {
            CleanupSuggestionEntity
                .list("status = ?1 and deleteAfter <= ?2", CleanupSuggestionEntity.STATUS_PENDING, now)
        }.awaitSuspending()

    override suspend fun find(id: UUID): CleanupSuggestionEntity? =
        Panache.withSession {
            CleanupSuggestionEntity.findById(id)
        }.awaitSuspending()

    override suspend fun findPendingByAnnouncementEvent(eventId: String): CleanupSuggestionEntity? =
        Panache.withSession {
            CleanupSuggestionEntity
                .find(
                    "status = ?1 and announcementEventId = ?2",
                    CleanupSuggestionEntity.STATUS_PENDING,
                    eventId,
                )
                .firstResult()
        }.awaitSuspending()

    override suspend fun save(entity: CleanupSuggestionEntity): CleanupSuggestionEntity =
        Panache.withTransaction {
            entity.persist<CleanupSuggestionEntity>()
        }.awaitSuspending()

    override suspend fun update(id: UUID, mutate: (CleanupSuggestionEntity) -> Unit): CleanupSuggestionEntity? =
        Panache.withTransaction {
            CleanupSuggestionEntity.findById(id)
                .invoke { entity ->
                    entity?.let {
                        mutate(it)
                        it.updatedAt = LocalDateTime.now()
                    }
                }
        }.awaitSuspending()
}
