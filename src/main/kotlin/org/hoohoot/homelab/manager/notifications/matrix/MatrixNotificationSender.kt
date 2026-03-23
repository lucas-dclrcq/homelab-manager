package org.hoohoot.homelab.manager.notifications.matrix

import jakarta.enterprise.context.ApplicationScoped

import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.RelatesTo
import net.folivo.trixnity.core.model.events.m.ReactionEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.NotificationId

@ApplicationScoped
class MatrixNotificationSender(
    private val matrixApiClient: MatrixClientServerApiClient,
    private val config: MatrixConfiguration
) {
    suspend fun sendMediaNotification(content: RoomMessageEventContent.TextBased.Text, relatedTo: NotificationId? = null): NotificationId =
        sendNotification(content, config.room().media(), relatedTo)

    suspend fun sendSupportNotification(content: RoomMessageEventContent.TextBased.Text, relatedTo: NotificationId? = null): NotificationId =
        sendNotification(content, config.room().support(), relatedTo)

    suspend fun sendMusicNotification(content: RoomMessageEventContent.TextBased.Text): NotificationId =
        sendNotification(content, config.room().music())

    suspend fun reactToSupportMessage(eventId: NotificationId, emoji: String) =
        sendReaction(eventId, config.room().support(), emoji)

    private suspend fun sendReaction(eventId: NotificationId, roomId: String, emoji: String) {
        val reactionContent = ReactionEventContent(
            relatesTo = RelatesTo.Annotation(
                eventId = EventId(eventId.value),
                key = emoji
            )
        )

        matrixApiClient.room.sendMessageEvent(
            roomId = RoomId(roomId),
            eventContent = reactionContent
        ).getOrThrow()
    }

    private suspend fun sendNotification(
        content: RoomMessageEventContent.TextBased.Text,
        roomId: String,
        relatedNotificationId: NotificationId? = null
    ): NotificationId {
        val relatesTo = relatedNotificationId?.let {
            RelatesTo.Thread(eventId = EventId(it.value))
        }

        val messageContent = if (relatesTo != null) content.copy(relatesTo = relatesTo) else content

        val eventId = matrixApiClient.room.sendMessageEvent(
            roomId = RoomId(roomId),
            eventContent = messageContent
        ).getOrThrow()

        return NotificationId(eventId.full)
    }
}
