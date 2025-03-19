package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.fuchss.matrix.bots.MatrixBot
import org.hoohoot.homelab.manager.application.queries.GetTopWatched
import org.hoohoot.homelab.manager.application.queries.TopWatchedPeriod

class TopWatchedMatrixCommand(private val mediator: Mediator) : BaseMatrixCommand() {
    override val name: String = "top-watched"
    override val help: String = "List the top watched medias for a period. (usage: !johnny top-watched <last-week|last-month|last-year>)"
    override val autoAcknowledge = true

    override suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val topWatchedPeriod = when (parameters) {
            "last-week" -> TopWatchedPeriod.LastWeek
            "last-month" -> TopWatchedPeriod.LastMonth
            "last-year" -> TopWatchedPeriod.LastYear
            else -> throw IllegalArgumentException("Unsupported period: $parameters")
        }

        val topWatched = mediator.send(GetTopWatched(topWatchedPeriod))

        val message = """
                <h1>Top watch for ${topWatched.period}</h1>
                <h2>Top watched series : </h2>
                <ol>
                    ${topWatched.series.joinToString("\n") { "<li>${it.name} - ${it.viewers} viewers</li>" }}
                </ol>
                <h2>Top watched movies : </h2>
                <ol>
                    ${topWatched.movies.joinToString("\n") { "<li>${it.name} - ${it.viewers} viewers</li>" }}
                </ol>
            """.trimIndent()

        matrixBot.room().sendMessage(roomId) { text(message, "org.matrix.custom.html", message) }
    }
}