package org.hoohoot.homelab.manager.notifications.matrix

import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.events.m.RelatesTo
import de.connect2x.trixnity.core.model.events.m.ReactionEventContent
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.NotificationId

suspend fun MatrixClientServerApiClient.sendNotification(
    content: RoomMessageEventContent.TextBased.Text,
    roomId: String,
    relatedTo: NotificationId? = null
): NotificationId {
    val relatesTo = relatedTo?.let {
        RelatesTo.Thread(eventId = EventId(it.value))
    }

    val messageContent = if (relatesTo != null) content.copy(relatesTo = relatesTo) else content

    val eventId = room.sendMessageEvent(
        roomId = RoomId(roomId),
        eventContent = messageContent
    ).getOrThrow()

    return NotificationId(eventId.full)
}

suspend fun MatrixClientServerApiClient.sendReaction(
    eventId: NotificationId,
    roomId: String,
    emoji: String
) {
    val reactionContent = ReactionEventContent(
        relatesTo = RelatesTo.Annotation(
            eventId = EventId(eventId.value),
            key = emoji
        )
    )

    room.sendMessageEvent(
        roomId = RoomId(roomId),
        eventContent = reactionContent
    ).getOrThrow()
}
