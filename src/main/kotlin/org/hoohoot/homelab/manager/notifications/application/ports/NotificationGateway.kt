package org.hoohoot.homelab.manager.notifications.application.ports

import org.hoohoot.homelab.manager.notifications.domain.Notification
import org.hoohoot.homelab.manager.notifications.domain.NotificationId

interface NotificationGateway {
    suspend fun sendMovieNotification(notification: Notification): NotificationId
    suspend fun sendSeriesNotification(notification: Notification): NotificationId
    suspend fun sendIssueNotification(notification: Notification): NotificationId
    suspend fun sendMusicNotification(notification: Notification): NotificationId
}