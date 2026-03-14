package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import jakarta.enterprise.context.ApplicationScoped

import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.RelatesTo
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.Notification
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationId

@ApplicationScoped
class MatrixNotificationGateway(
    private val matrixApiClient: MatrixClientServerApiClient,
    private val config: MatrixConfiguration
) : NotificationGateway {
    override suspend fun sendMediaNotification(notification: Notification, relatedTo: NotificationId?): NotificationId =
        sendNotification(notification, config.room().media(), relatedTo)

    override suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId?): NotificationId =
        sendNotification(notification, config.room().support(), relatedTo)

    override suspend fun sendMusicNotification(notification: Notification): NotificationId =
        sendNotification(notification, config.room().music())

    private suspend fun sendNotification(
        notification: Notification,
        roomId: String,
        relatedNotificationId: NotificationId? = null
    ): NotificationId {
        val relatesTo = relatedNotificationId?.let {
            RelatesTo.Thread(eventId = EventId(it.value))
        }

        val content = RoomMessageEventContent.TextBased.Text(
            body = notification.textMessage,
            format = "org.matrix.custom.html",
            formattedBody = notification.htmlMessage,
            relatesTo = relatesTo
        )

        val eventId = matrixApiClient.room.sendMessageEvent(
            roomId = RoomId(roomId),
            eventContent = content
        ).getOrThrow()

        return NotificationId(eventId.full)
    }
}
