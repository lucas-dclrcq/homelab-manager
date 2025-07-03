package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.queries.Ping
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBotCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot

@ApplicationScoped
class PingMatrixCommand(private val mediator: Mediator) : MatrixBotCommand() {
    override val name: String = "ping"
    override val help: String = "Ensure the bot is working. It will reply with 'Pong!'."
    override val autoAcknowledge = true

    override suspend fun execute(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val pong = mediator.send(Ping)
        matrixBot.room().sendMessage(roomId) { text(pong) }
    }
}