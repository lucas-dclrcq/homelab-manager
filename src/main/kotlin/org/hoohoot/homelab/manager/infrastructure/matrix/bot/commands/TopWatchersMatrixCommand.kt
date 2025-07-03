package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.queries.GetTopWatchers
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot

@ApplicationScoped
class TopWatchersMatrixCommand(private val mediator: Mediator) : BaseMatrixCommand() {
    override val name: String = "top-watchers"
    override val help: String = "List the top 10 watchers on Hoohoot (usage: !johnny top-watchers)"
    override val autoAcknowledge = true

    override suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {


        val topWatchers = mediator.send(GetTopWatchers(10))

        val message = """
                <h2>🏆 Top ten watchers on hoohoot 🏆</h2>
                <ol>
                    ${topWatchers.joinToString("\n") { "<li>${it.username} - ${it.totalPlaybackInHours} playback (${it.totalPlays} total plays)</li>" }}
                </ol>
            """.trimIndent()

        matrixBot.room().sendMessage(roomId) { text(message, "org.matrix.custom.html", message) }
    }
}