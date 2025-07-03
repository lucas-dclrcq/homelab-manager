package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands

import io.quarkus.logging.Log
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.emoji

abstract class PrefixedBotCommand : MatrixBotCommand()

abstract class RegexBotCommand : MatrixBotCommand() {
    abstract val regex: Regex

    fun matches(text: String): Boolean = regex.matches(text)
}

abstract class MatrixBotCommand {
    abstract val name: String
    open val params: String = ""
    abstract val help: String
    open val autoAcknowledge: Boolean = false

    companion object {
        @JvmStatic
        val ACK_EMOJI = ":heavy_check_mark:".emoji()
    }

    protected abstract suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    )

    suspend fun execute(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        try {
            this.handle(matrixBot, sender, roomId, parameters, textEventId, textEvent)
        } catch (e: Exception) {
            Log.error("Failed to respond to $name matrix command", e)
            matrixBot.room().sendMessage(roomId) { text("An error occurred : ${e.message}") }
        }
    }
}