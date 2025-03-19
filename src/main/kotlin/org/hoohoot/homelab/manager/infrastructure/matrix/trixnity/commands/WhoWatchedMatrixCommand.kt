package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.fuchss.matrix.bots.MatrixBot
import org.hoohoot.homelab.manager.application.queries.WhoWatched

@ApplicationScoped
class WhoWatchedMatrixCommand(private val mediator: Mediator) : BaseMatrixCommand() {
    override val name: String = "who-watched"
    override val help: String =
        "Find out who watched last episode of a TV show. (usage: !johnny who-watched <show name>)"
    override val autoAcknowledge = true

    override suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val whoWatched = mediator.send(WhoWatched(parameters))

        val body = """
            ${whoWatched.watchersCount} people watched ${whoWatched.tvShow} :
            ${whoWatched.watchers.joinToString("\n") { "- ${it.username} watched ${it.episodeWatchedCount} episodes (latest: ${it.lastEpisodeWatched})" }}
        """.trimIndent()

        matrixBot.room().sendMessage(roomId) { text(body) }
    }
}