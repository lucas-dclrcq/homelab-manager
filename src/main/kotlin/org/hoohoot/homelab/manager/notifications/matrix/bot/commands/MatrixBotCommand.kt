package org.hoohoot.homelab.manager.notifications.matrix.bot.commands

import io.quarkus.logging.Log
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.emoji

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
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    )

    suspend fun execute(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        try {
            this.handle(session, sender, roomId, parameters, textEventId, textEvent)
        } catch (e: Exception) {
            Log.error("Failed to respond to $name matrix command", e)
            session.room.sendMessage(roomId) { text("An error occurred : ${e.message}") }
        }
    }
}
