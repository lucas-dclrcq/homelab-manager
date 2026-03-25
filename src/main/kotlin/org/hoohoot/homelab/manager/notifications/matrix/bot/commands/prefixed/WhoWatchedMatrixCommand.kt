package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.jellyfin.JellyfinRestClient
import org.hoohoot.homelab.manager.notifications.jellyfin.searchSeries
import org.hoohoot.homelab.manager.notifications.jellystat.JellystatService
import org.hoohoot.homelab.manager.media.MultipleSeriesFoundException
import org.hoohoot.homelab.manager.media.NoSeriesFoundException
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class WhoWatchedMatrixCommand(
    private val jellystatService: JellystatService,
    @param:RestClient private val jellyfinRestClient: JellyfinRestClient
) : PrefixedBotCommand() {
    override val name: String = "who-watched"
    override val help: String =
        "Find out who watched last episode of a TV show. (usage: !johnny who-watched <show name>)"
    override val autoAcknowledge = true

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Finding out who watched $parameters")

        val media = jellyfinRestClient.searchSeries(parameters)

        if (media.isEmpty()) {
            throw NoSeriesFoundException("No series found for '$parameters'")
        }

        if (media.size > 1) {
            throw MultipleSeriesFoundException("Multiple series found for '$parameters' : ${media.joinToString(",") { it.name }}. Please be more specific.")
        }

        val firstFoundMedia = media.first()
        val whoWatched = jellystatService.getWatchersInfo(firstFoundMedia.itemId, firstFoundMedia.name)

        val body = """
            <h1>📺 Who watched last episode of ${whoWatched.tvShow} ? (${whoWatched.watchersCount} watchers)</h1>
            <ol>
                ${whoWatched.watchers.joinToString("\n") { "<li>${it.username} watched ${it.episodeWatchedCount} episodes (latest: ${it.lastEpisodeWatched})</li>" }}
            </ol>
        """.trimIndent()

        session.room.sendMessage(roomId) { text(body, "org.matrix.custom.html", body ) }
    }
}
