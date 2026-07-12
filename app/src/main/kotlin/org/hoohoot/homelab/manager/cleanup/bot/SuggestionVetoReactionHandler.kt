package org.hoohoot.homelab.manager.cleanup.bot

import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import io.quarkus.logging.Log
import io.vertx.core.Vertx
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.usecases.VetoSuggestionByReaction
import org.hoohoot.homelab.manager.cleanup.infra.MatrixVetoReactionsAdapter
import org.hoohoot.homelab.manager.shared.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.shared.matrix.bot.reactions.ReactionBotHandler
import org.hoohoot.homelab.manager.shared.vertx.runOnSafeVertxContext

// Une réaction ❌ sur l'annonce d'une suggestion vaut veto immédiat : l'issue est
// annoncée dans le chat sans attendre l'échéance
@ApplicationScoped
class SuggestionVetoReactionHandler(
    private val vetoSuggestionByReaction: VetoSuggestionByReaction,
    private val vertx: Vertx,
) : ReactionBotHandler {

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        targetEventId: EventId,
        key: String,
    ) {
        if (!MatrixVetoReactionsAdapter.isVetoEmoji(key)) return

        // Les handlers bot tournent sur les dispatchers trixnity : Panache exige un contexte Vertx safe
        val vetoed = runOnSafeVertxContext(vertx) {
            vetoSuggestionByReaction(targetEventId.full, sender.localpart)
        }

        if (vetoed != null) {
            Log.info("Cleanup: suggestion '${vetoed.displayTitle()}' vetoed by ${sender.localpart} via reaction")
        }
    }
}
