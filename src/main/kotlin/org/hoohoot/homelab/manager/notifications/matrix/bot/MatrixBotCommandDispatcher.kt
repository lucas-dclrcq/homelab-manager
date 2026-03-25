package org.hoohoot.homelab.manager.notifications.matrix.bot

import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.react
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.*
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.MatrixBotCommand
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed.PrefixedBotCommands
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex.RegexBotCommands

@ApplicationScoped
class MatrixBotCommandDispatcher(
    private val session: MatrixBotSession,
    private val prefixedBotCommands: PrefixedBotCommands,
    private val regexBotCommands: RegexBotCommands,
    private val config: MatrixBotConfiguration
) {

    suspend fun dispatch(event: ClientEvent<RoomMessageEventContent>) {
        val roomId = event.roomIdOrNull ?: return
        val sender = event.senderOrNull ?: return
        val eventId = event.idOrNull ?: return
        val content = event.content
        if (content is RoomMessageEventContent.TextBased.Text) {
            val hasExecutedPrefixedCommand = executePrefixedCommand(sender, roomId, eventId, content)

            if (!hasExecutedPrefixedCommand) {
                executeRegexCommand(sender, roomId, eventId, content)
            }
        }
    }

    private suspend fun executePrefixedCommand(
        sender: UserId,
        roomId: RoomId,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ): Boolean {
        var message = textEvent.body
        if (!message.startsWith("!${config.prefix()}")) {
            return false
        }
        message = message.substring("!${config.prefix()}".length).trim()

        val command = message.split(Regex(" "), 2)[0]
        val parameters = message.substring(command.length).trim()

        val commandToExecute = prefixedBotCommands.find(command) ?: return false

        if (commandToExecute.autoAcknowledge) {
            session.room.sendMessage(roomId) {
                react(textEventId, MatrixBotCommand.ACK_EMOJI)
            }
        }

        commandToExecute.execute(session, sender, roomId, parameters, textEventId, textEvent)
        return true
    }

    private suspend fun executeRegexCommand(
        sender: UserId,
        roomId: RoomId,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val message = textEvent.body

        val commandToExecute = regexBotCommands.find(message) ?: return

        if (commandToExecute.autoAcknowledge) {
            session.room.sendMessage(roomId) {
                react(textEventId, MatrixBotCommand.ACK_EMOJI)
            }
        }

        commandToExecute.execute(session, sender, roomId, message, textEventId, textEvent)
    }
}
