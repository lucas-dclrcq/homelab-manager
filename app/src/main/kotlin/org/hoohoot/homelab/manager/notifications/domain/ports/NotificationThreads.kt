package org.hoohoot.homelab.manager.notifications.domain.ports

import org.hoohoot.homelab.manager.notifications.domain.NotificationId

interface NotificationThreads {
    suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId)
    suspend fun getNotificationIdForIssue(issueId: String): NotificationId?
    suspend fun getThreadByMediaId(mediaId: String, mediaType: String): NotificationId?
    suspend fun getThreadByMediaKey(mediaKey: String): NotificationId?
    suspend fun saveOrUpdateThread(mediaId: String, mediaType: String, mediaKey: String?, notificationId: NotificationId)
    suspend fun deleteExpiredThreads()
}
