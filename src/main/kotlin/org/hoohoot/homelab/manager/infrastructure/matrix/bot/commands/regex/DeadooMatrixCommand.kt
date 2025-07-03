package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.regex

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.queries.CestComment
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.RegexBotCommand

@ApplicationScoped
class DeadooMatrixCommand(private val mediator: Mediator): RegexBotCommand() {
    override val name: String = "deadoo"
    override val help: String = ""
    override val autoAcknowledge = false
    override val regex: Regex = Regex(".*c'est comment.*", RegexOption.IGNORE_CASE)

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val cestDeadooo = mediator.send(CestComment)
        matrixBot.room().sendMessage(roomId) { text(cestDeadooo) }
    }
}