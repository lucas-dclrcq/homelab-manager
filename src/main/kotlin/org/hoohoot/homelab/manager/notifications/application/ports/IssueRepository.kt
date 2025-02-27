package org.hoohoot.homelab.manager.notifications.application.ports

import org.hoohoot.homelab.manager.notifications.domain.NotificationId

interface IssueRepository {
    suspend fun saveNotificationIdForIssue(issueId: String, notification: NotificationId)
    suspend fun getNotificationIdForIssue(issueId: String): NotificationId?
}