package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.fuchss.matrix.bots.MatrixBot
import org.fuchss.matrix.bots.command.Command
import org.hoohoot.homelab.manager.application.queries.GetSkong
import org.hoohoot.homelab.manager.application.queries.Ping
import org.hoohoot.homelab.manager.application.queries.SkongType

@ApplicationScoped
class SkongMatrixCommand(private val mediator: Mediator) : BaseMatrixCommand() {
    override val name: String = "skong"
    override val help: String = "Skong! (usage: !johnny skong <believer|doubter>)"
    override val autoAcknowledge = true

    override suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val skongType = when (parameters) {
            "believer","beleiver" -> SkongType.Believer
            "doubter" -> SkongType.Doubter
            else -> throw IllegalArgumentException("Unsupported skong: $parameters")
        }

        val skongResponse = mediator.send(GetSkong(skongType))
        matrixBot.room().sendMessage(roomId) { text(skongResponse.message, skongResponse.message, "org.matrix.custom.html") }
    }
}