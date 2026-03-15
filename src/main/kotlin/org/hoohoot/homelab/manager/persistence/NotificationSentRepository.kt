package org.hoohoot.homelab.manager.persistence

import io.quarkus.hibernate.reactive.panache.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.NotificationId
import java.time.LocalDateTime

@ApplicationScoped
class NotificationSentRepository {

    suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId) {
        Panache.withTransaction {
            val entity = NotificationSentEntity(issueId, notification.value)
            Panache.getSession().flatMap { session -> session.merge(entity) }
        }.awaitSuspending()
    }

    suspend fun getNotificationIdForIssue(issueId: String): NotificationId? {
        val entity = Panache.withSession {
            NotificationSentEntity.findById(issueId)
        }.awaitSuspending()
        return entity?.let { NotificationId(it.eventId) }
    }

    suspend fun getActiveThreadForSeries(seriesId: String): NotificationId? {
        val entity = Panache.withSession {
            SeriesNotificationThreadEntity.findById(seriesId)
        }.awaitSuspending()

        if (entity == null) return null
        val cutoff = LocalDateTime.now().minusHours(24)
        if (entity.lastNotifiedAt.isBefore(cutoff)) return null

        return NotificationId(entity.eventId)
    }

    suspend fun saveOrUpdateThreadForSeries(seriesId: String, notificationId: NotificationId) {
        Panache.withTransaction {
            val entity = SeriesNotificationThreadEntity(seriesId, notificationId.value, LocalDateTime.now())
            Panache.getSession().flatMap { session -> session.merge(entity) }
        }.awaitSuspending()
    }
}
