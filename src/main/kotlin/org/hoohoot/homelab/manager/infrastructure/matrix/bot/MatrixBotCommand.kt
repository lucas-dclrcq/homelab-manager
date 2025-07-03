package org.hoohoot.homelab.manager.infrastructure.matrix.bot

import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent

abstract class MatrixBotCommand {
    abstract val name: String
    open val params: String = ""
    abstract val help: String
    open val autoAcknowledge: Boolean = false

    companion object {
        @JvmStatic
        val ACK_EMOJI = ":heavy_check_mark:".emoji()
    }

    /**
     * Execute the command.
     * @param[matrixBot] The bot to execute the command.
     * @param[sender] The sender of the command.
     * @param[roomId] The room to execute the command in.
     * @param[parameters] The parameters of the command.
     * @param[textEventId] The text event id of the command.
     * @param[textEvent] The text event of the command.
     */
    abstract suspend fun execute(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    )
}
