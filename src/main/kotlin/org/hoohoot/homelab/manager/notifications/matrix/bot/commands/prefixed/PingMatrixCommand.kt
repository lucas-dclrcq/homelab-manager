package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class PingMatrixCommand : PrefixedBotCommand() {
    override val name: String = "ping"
    override val help: String = "Ensure the bot is working. It will reply with 'Pong!'."
    override val autoAcknowledge = true

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        matrixBot.room().sendMessage(roomId) { text("Pong!") }
    }
}
