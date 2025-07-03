package org.hoohoot.homelab.manager.application.ports.notifications

import org.hoohoot.homelab.manager.domain.media_notifications.Notification
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId

interface NotificationGateway {
    suspend fun sendMediaNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId
    suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId
    suspend fun sendMusicNotification(notification: Notification): NotificationId
}