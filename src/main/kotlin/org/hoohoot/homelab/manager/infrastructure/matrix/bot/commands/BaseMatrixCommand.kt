package org.hoohoot.homelab.manager.infrastructure.matrix.trixnity.commands

import io.quarkus.logging.Log
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBotCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot

abstract class BaseMatrixCommand : MatrixBotCommand() {
    abstract suspend fun executeCatching(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    )

    override suspend fun execute(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        try {
           this.executeCatching(matrixBot, sender, roomId, parameters, textEventId, textEvent)
        } catch (e: Exception) {
            Log.error("Failed to respond to $name matrix command", e)
            matrixBot.room().sendMessage(roomId) {text("An error occurred : ${e.message}")}
        }
    }
}