package org.hoohoot.homelab.manager.infrastructure.persistence

import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationRepository
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId

@ApplicationScoped
class PostgresNotificationRepository : NotificationRepository {

    override suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId) {
        Panache.withTransaction {
            val entity = NotificationSentEntity(issueId, notification.value)
            Panache.getSession().flatMap { session -> session.merge(entity) }
        }.awaitSuspending()
    }

    override suspend fun getNotificationIdForIssue(issueId: String): NotificationId? {
        val entity = Panache.withSession {
            NotificationSentEntity.findById(issueId)
        }.awaitSuspending()
        return entity?.let { NotificationId(it.eventId) }
    }
}
