package org.hoohoot.homelab.manager.notifications.domain.ports

import org.hoohoot.homelab.manager.notifications.domain.NotificationId
import org.hoohoot.homelab.manager.notifications.domain.NotificationMessage
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom

interface NotificationSender {
    suspend fun send(
        room: NotificationRoom,
        message: NotificationMessage,
        inThread: NotificationId? = null,
    ): NotificationId

    suspend fun react(room: NotificationRoom, to: NotificationId, emoji: String)
}
