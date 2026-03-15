package org.hoohoot.homelab.manager.matrix.bot.commands.prefixed

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.LocalDate
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.time.TimeService
import org.hoohoot.homelab.manager.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.matrix.bot.commands.PrefixedBotCommand

@ApplicationScoped
class SkongMatrixCommand(private val timeService: TimeService) : PrefixedBotCommand() {
    override val name: String = "skong"
    override val help: String = "Skong! (usage: !johnny skong <believer|doubter>)"
    override val autoAcknowledge = true

    private val skongOrigin = LocalDate.parse("2019-02-14")

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        val daysSince = timeService.getDaysSince(skongOrigin)

        val skongMessage = when (parameters) {
            "believer", "beleiver" -> """🟢 Patience, my child. Silksong will come when it is ready. The longer the wait, the greater the masterpiece!"""
            "doubter" -> """🔴 It's been $daysSince days, and there is still no release date. Face it, Silksong is never coming out. Team Cherry is just a myth."""
            else -> throw IllegalArgumentException("Unsupported skong: $parameters")
        }

        matrixBot.room().sendMessage(roomId) { text(skongMessage, skongMessage, "org.matrix.custom.html") }
    }
}
