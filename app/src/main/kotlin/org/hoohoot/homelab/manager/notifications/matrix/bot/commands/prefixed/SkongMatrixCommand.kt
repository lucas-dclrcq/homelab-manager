package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.prefixed

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.LocalDate
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.PrefixedBotCommand
import org.hoohoot.homelab.manager.time.TimeService

@ApplicationScoped
class SkongMatrixCommand(private val timeService: TimeService) : PrefixedBotCommand() {
    override val name: String = "skong"
    override val help: String = "Skong! (usage: !johnny skong <believer|doubter>)"
    override val autoAcknowledge = true

    private val skongOrigin = LocalDate.parse("2019-02-14")

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        Log.info("Skong command requested by ${sender.localpart} as $parameters")
        val daysSince = timeService.getDaysSince(skongOrigin)

        val skongMessage = when (parameters) {
            "believer", "beleiver" -> """🟢 Patience, my child. Silksong will come when it is ready. The longer the wait, the greater the masterpiece!"""
            "doubter" -> """🔴 It's been $daysSince days, and there is still no release date. Face it, Silksong is never coming out. Team Cherry is just a myth."""
            else -> throw IllegalArgumentException("Unsupported skong: $parameters")
        }

        session.room.sendMessage(roomId) { text(skongMessage, skongMessage, "org.matrix.custom.html") }
    }
}
