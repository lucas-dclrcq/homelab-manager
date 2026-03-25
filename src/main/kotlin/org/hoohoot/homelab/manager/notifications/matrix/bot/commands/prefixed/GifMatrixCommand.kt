package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.ktor.http.*
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.image
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.utils.toByteArrayFlow
import org.hoohoot.homelab.manager.notifications.giphy.GiphyService
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class GifMatrixCommand(private val giphyService: GiphyService) : PrefixedBotCommand() {
    override val name: String = "gif"
    override val help: String = "Sends a gif relatives to the search term. (usage: !johnny gif <search term>)"
    override val autoAcknowledge = true

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Gif command requested by ${sender.localpart} with query: $parameters")
        val gif = giphyService.searchGif(parameters)
        session.room.sendMessage(roomId) {
            image(
                body = "${parameters}.gif",
                image = gif.file.toByteArrayFlow(),
                type = ContentType.Image.GIF
            )
        }
    }
}
