package org.hoohoot.homelab.manager.notifications.matrix

import jakarta.enterprise.context.ApplicationScoped

import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.RelatesTo
import net.folivo.trixnity.core.model.events.m.ReactionEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.Notification
import org.hoohoot.homelab.manager.notifications.NotificationId

@ApplicationScoped
class MatrixNotificationSender(
    private val matrixApiClient: MatrixClientServerApiClient,
    private val config: MatrixConfiguration
) {
    suspend fun sendMediaNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId =
        sendNotification(notification, config.room().media(), relatedTo)

    suspend fun sendSupportNotification(notification: Notification, relatedTo: NotificationId? = null): NotificationId =
        sendNotification(notification, config.room().support(), relatedTo)

    suspend fun sendMusicNotification(notification: Notification): NotificationId =
        sendNotification(notification, config.room().music())

    suspend fun reactToSupportMessage(eventId: NotificationId, emoji: String) =
        sendReaction(eventId, config.room().support(), emoji)

    private suspend fun sendReaction(eventId: NotificationId, roomId: String, emoji: String) {
        val content = ReactionEventContent(
            relatesTo = RelatesTo.Annotation(
                eventId = EventId(eventId.value),
                key = emoji
            )
        )

        matrixApiClient.room.sendMessageEvent(
            roomId = RoomId(roomId),
            eventContent = content
        ).getOrThrow()
    }

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
