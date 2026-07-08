package org.hoohoot.homelab.manager.notifications.infra.matrix

import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.events.m.ReactionEventContent
import de.connect2x.trixnity.core.model.events.m.RelatesTo
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationId
import org.hoohoot.homelab.manager.notifications.domain.NotificationMessage
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.shared.matrix.MatrixRoomProvider

@ApplicationScoped
class MatrixNotificationSender(
    private val matrixClient: MatrixClientServerApiClient,
    private val roomProvider: MatrixRoomProvider,
) : NotificationSender {

    override suspend fun send(
        room: NotificationRoom,
        message: NotificationMessage,
        inThread: NotificationId?,
    ): NotificationId {
        val content = RoomMessageEventContent.TextBased.Text(
            body = message.body,
            format = "org.matrix.custom.html",
            formattedBody = message.formattedBody,
            relatesTo = inThread?.let { RelatesTo.Thread(eventId = EventId(it.value)) },
        )
        val eventId = matrixClient.room.sendMessageEvent(
            roomId = RoomId(room.resolve()),
            eventContent = content,
        ).getOrThrow()
        return NotificationId(eventId.full)
    }

    override suspend fun react(room: NotificationRoom, to: NotificationId, emoji: String) {
        val reactionContent = ReactionEventContent(
            relatesTo = RelatesTo.Annotation(eventId = EventId(to.value), key = emoji),
        )
        matrixClient.room.sendMessageEvent(
            roomId = RoomId(room.resolve()),
            eventContent = reactionContent,
        ).getOrThrow()
    }

    private fun NotificationRoom.resolve(): String = when (this) {
        NotificationRoom.MEDIA -> roomProvider.media
        NotificationRoom.MUSIC -> roomProvider.music
        NotificationRoom.SUPPORT -> roomProvider.support
    }
}
