package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatService
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class TopWatchersMatrixCommand(private val jellystatService: JellystatService) : PrefixedBotCommand() {
    override val name: String = "top-watchers"
    override val help: String = "List the top 10 watchers on Hoohoot (usage: !johnny top-watchers)"
    override val autoAcknowledge = true

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Top-watchers command requested by ${sender.localpart}")
        val topWatchers = jellystatService.getTopWatchers(10)

        val message = """
                <h2>🏆 Top ten watchers on hoohoot 🏆</h2>
                <ol>
                    ${topWatchers.joinToString("\n") { "<li>${it.username} - ${it.totalPlaybackInHours} playback (${it.totalPlays} total plays)</li>" }}
                </ol>
            """.trimIndent()

        session.room.sendMessage(roomId) { text(message, "org.matrix.custom.html", message) }
    }
}
