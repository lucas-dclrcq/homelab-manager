package org.hoohoot.homelab.manager.notifications.infra.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationId
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationThreads
import java.time.LocalDateTime

@ApplicationScoped
class NotificationSentRepository : NotificationThreads {

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

    override suspend fun getThreadByMediaId(mediaId: String, mediaType: String): NotificationId? {
        val entity = Panache.withSession {
            MediaNotificationThreadEntity.find("mediaId = ?1 and mediaType = ?2", mediaId, mediaType)
                .firstResult()
        }.awaitSuspending()
        return entity?.let { NotificationId(it.eventId) }
    }

    override suspend fun getThreadByMediaKey(mediaKey: String): NotificationId? {
        val entity = Panache.withSession {
            MediaNotificationThreadEntity.find("mediaKey", mediaKey).firstResult()
        }.awaitSuspending()
        return entity?.let { NotificationId(it.eventId) }
    }

    override suspend fun saveOrUpdateThread(mediaId: String, mediaType: String, mediaKey: String?, notificationId: NotificationId) {
        Panache.withTransaction {
            MediaNotificationThreadEntity.find("mediaId = ?1 and mediaType = ?2", mediaId, mediaType)
                .firstResult()
                .flatMap { existing ->
                    val entity = existing ?: MediaNotificationThreadEntity()
                    entity.mediaId = mediaId
                    entity.mediaType = mediaType
                    entity.mediaKey = mediaKey
                    entity.eventId = notificationId.value
                    entity.lastNotifiedAt = LocalDateTime.now()
                    Panache.getSession().flatMap { session -> session.merge(entity) }
                }
        }.awaitSuspending()
    }

    override suspend fun deleteExpiredThreads() {
        val cutoff = LocalDateTime.now().minusHours(48)
        Panache.withTransaction {
            MediaNotificationThreadEntity.delete("lastNotifiedAt < ?1", cutoff)
        }.awaitSuspending()
    }
}
