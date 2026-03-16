package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.ktor.http.*
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.image
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.utils.toByteArrayFlow
import org.hoohoot.homelab.manager.notifications.giphy.GiphyService
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class GifMatrixCommand(private val giphyService: GiphyService) : PrefixedBotCommand() {
    override val name: String = "gif"
    override val help: String = "Sends a gif relatives to the search term. (usage: !johnny gif <search term>)"
    override val autoAcknowledge = true

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val gif = giphyService.searchGif(parameters)
        matrixBot.room().sendMessage(roomId) {
            image(
                body = "${parameters}.gif",
                image = gif.file.toByteArrayFlow(),
                type = ContentType.Image.GIF
            )
        }
    }
}
