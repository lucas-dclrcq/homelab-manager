package org.hoohoot.homelab.manager.application.ports.notifications

import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId

interface NotificationRepository {
    suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId)
    suspend fun getNotificationIdForIssue(issueId: String): NotificationId?
}