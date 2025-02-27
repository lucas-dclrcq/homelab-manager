package org.hoohoot.homelab.manager.notifications.application.ports

import org.hoohoot.homelab.manager.notifications.domain.Notification
import org.hoohoot.homelab.manager.notifications.domain.NotificationId

interface NotificationGateway {
    suspend fun sendMediaNotification(notification: Notification): NotificationId
    suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId
    suspend fun sendMusicNotification(notification: Notification): NotificationId
}