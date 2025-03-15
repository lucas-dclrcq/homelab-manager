package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.fuchss.matrix.bots.MatrixBot
import org.fuchss.matrix.bots.command.Command
import org.hoohoot.homelab.manager.application.queries.WhoWatchedTvShow

class WhoWatchedCommand(private val mediator: Mediator) : Command() {
    override val name: String = "who-watched"
    override val help: String = "Find out who watched last episode of a TV show."
    override val autoAcknowledge = true

    override suspend fun execute(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val whoWhatchedResult = mediator.send(WhoWatchedTvShow(parameters))
        matrixBot.room().sendMessage(roomId) { text(whoWhatchedResult.joinToString()) }
    }
}