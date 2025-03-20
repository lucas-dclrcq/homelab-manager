package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import com.trendyol.kediatr.Mediator
import io.ktor.http.*
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.image
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import net.folivo.trixnity.utils.toByteArrayFlow
import org.fuchss.matrix.bots.MatrixBot
import org.hoohoot.homelab.manager.application.queries.GetGif

@ApplicationScoped
class GifMatrixCommand(private val mediator: Mediator) : BaseMatrixCommand() {
    override val name: String = "gif"
    override val help: String = "Sends a gif relatives to the search term. (usage: !johnny gif <search term>)"

    override suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val gif = this.mediator.send(GetGif(parameters))
        matrixBot.room().sendMessage(roomId) {
            image(
                body = "${parameters}.gif",
                image = gif.file.toByteArrayFlow(),
                type = ContentType.Image.GIF
            )
        }
    }
}