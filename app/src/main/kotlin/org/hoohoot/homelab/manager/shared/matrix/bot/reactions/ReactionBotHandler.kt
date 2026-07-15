package org.hoohoot.homelab.manager.shared.matrix.bot.reactions

import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import org.hoohoot.homelab.manager.shared.matrix.bot.MatrixBotSession

interface ReactionBotHandler {
    suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        targetEventId: EventId,
        key: String,
    )
}
