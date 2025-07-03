package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.prefixed

import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBotConfiguration
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.MatrixBotCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.PrefixedBotCommand
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.markdown

class HelpCommand(
    private val config: MatrixBotConfiguration,
    private val botName: String,
    private val commandGetter: () -> List<MatrixBotCommand>
) : PrefixedBotCommand() {
    override val name: String = "help"
    override val help: String = "shows this help message"

    /**
     * Show the help message.
     * @param[matrixBot] The bot to show the help message.
     * @param[sender] The sender of the command.
     * @param[roomId] The room to show the help message in.
     * @param[parameters] The parameters of the command.
     * @param[textEventId] The event of the command.
     * @param[textEvent] The event of the command.
     */
    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        var helpMessage = "This is $botName. You can use the following commands:\n"

        for (command in commandGetter()) {
            helpMessage += "\n* `!${config.prefix()} ${command.name} ${command.params} - ${command.help}`"
        }

        matrixBot.room().sendMessage(roomId) { markdown(helpMessage) }
    }
}