package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatService
import org.hoohoot.homelab.manager.media.TopWatchedPeriod
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class TopWatchedMatrixCommand(private val jellystatService: JellystatService) : PrefixedBotCommand() {
    override val name: String = "top-watched"
    override val help: String = "List the top watched medias for a period. (usage: !johnny top-watched <last-week|last-month|last-year>)"
    override val autoAcknowledge = true

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Top-watched command requested by ${sender.localpart} for period: $parameters")
        val topWatchedPeriod = when (parameters) {
            "last-week" -> TopWatchedPeriod.LastWeek
            "last-month" -> TopWatchedPeriod.LastMonth
            "last-year" -> TopWatchedPeriod.LastYear
            else -> throw IllegalArgumentException("Unsupported period: $parameters")
        }

        val topWatched = jellystatService.getTopWatched(topWatchedPeriod)

        val message = """
                <h1>🥇 Top watch for ${topWatched.period} 🥇</h1>
                <h2>📺 Most popular series : </h2>
                <ol>
                    ${topWatched.mostPopularSeries.joinToString("\n") { "<li>${it.name} - ${it.uniqueViewers} unique viewers</li>" }}
                </ol>
                <h2>🎬 Most popular movies : </h2>
                <ol>
                    ${topWatched.mostPopularMovies.joinToString("\n") { "<li>${it.name} - ${it.uniqueViewers} unique viewers</li>" }}
                </ol>
                <h2>📺 Most watched series : </h2>
                <ol>
                    ${topWatched.mostViewedSeries.joinToString("\n") { "<li>${it.name} - ${it.plays} plays (${it.totalPlaybackInHours})</li>" }}
                </ol>
                <h2>🎬 Most watched movies : </h2>
                <ol>
                    ${topWatched.mostViewedMovies.joinToString("\n") { "<li>${it.name} - ${it.plays} plays (${it.totalPlaybackInHours})</li>" }}
                </ol>
            """.trimIndent()

        session.room.sendMessage(roomId) { text(message, "org.matrix.custom.html", message) }
    }
}
