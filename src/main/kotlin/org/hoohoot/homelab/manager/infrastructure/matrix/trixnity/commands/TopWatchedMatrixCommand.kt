package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.fuchss.matrix.bots.MatrixBot
import org.hoohoot.homelab.manager.application.queries.GetTopWatched
import org.hoohoot.homelab.manager.application.queries.TopWatchedPeriod

@ApplicationScoped
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
                <h2>Most popular series : </h2>
                <ol>
                    ${topWatched.mostPopularSeries.joinToString("\n") { "<li>${it.name} - ${it.uniqueViewers} unique viewers</li>" }}
                </ol>
                <h2>Most popular movies : </h2>
                <ol>
                    ${topWatched.mostPopularMovies.joinToString("\n") { "<li>${it.name} - ${it.uniqueViewers} unique viewers</li>" }}
                </ol>
                <h2>Most watched series : </h2>
                <ol>
                    ${topWatched.mostViewedSeries.joinToString("\n") { "<li>${it.name} - ${it.plays} plays (${it.totalPlaybackInHours})</li>" }}
                </ol>
                <h2>Most watched movies : </h2>
                <ol>
                    ${topWatched.mostViewedMovies.joinToString("\n") { "<li>${it.name} - ${it.plays} plays (${it.totalPlaybackInHours})</li>" }}
                </ol>
            """.trimIndent()

        matrixBot.room().sendMessage(roomId) { text(message, "org.matrix.custom.html", message) }
    }
}