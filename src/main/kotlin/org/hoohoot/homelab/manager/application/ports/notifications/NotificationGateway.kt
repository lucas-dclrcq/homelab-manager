package org.hoohoot.homelab.manager.application.ports.notifications

import org.hoohoot.homelab.manager.domain.Notification
import org.hoohoot.homelab.manager.domain.NotificationId

interface NotificationGateway {
    suspend fun sendMediaNotification(notification: Notification): NotificationId
    suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId
    suspend fun sendMusicNotification(notification: Notification): NotificationId
}