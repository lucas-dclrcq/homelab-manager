package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class PingMatrixCommand : PrefixedBotCommand() {
    override val name: String = "ping"
    override val help: String = "Ensure the bot is working. It will reply with 'Pong!'."
    override val autoAcknowledge = true

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Ping command received from ${sender.localpart}, responding with Pong!")
        session.room.sendMessage(roomId) { text("Pong!") }
    }
}
