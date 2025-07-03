package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.prefixed

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.queries.WhoWatched
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class WhoWatchedMatrixCommand(private val mediator: Mediator) : PrefixedBotCommand() {
    override val name: String = "who-watched"
    override val help: String =
        "Find out who watched last episode of a TV show. (usage: !johnny who-watched <show name>)"
    override val autoAcknowledge = true

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val whoWatched = mediator.send(WhoWatched(parameters))

        val body = """
            <h1>📺 Who watched last episode of ${whoWatched.tvShow} ? (${whoWatched.watchersCount} watchers)</h1>
            <ol>
                ${whoWatched.watchers.joinToString("\n") { "<li>${it.username} watched ${it.episodeWatchedCount} episodes (latest: ${it.lastEpisodeWatched})</li>" }}
            </ol>
        """.trimIndent()

        matrixBot.room().sendMessage(roomId) { text(body, "org.matrix.custom.html", body ) }
    }
}