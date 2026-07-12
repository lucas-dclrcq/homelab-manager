package org.hoohoot.homelab.manager.shared.matrix.bot.reactions

import io.quarkus.arc.All
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ReactionBotHandlers(
    @param:All private val handlers: MutableList<ReactionBotHandler>,
) {
    fun all(): List<ReactionBotHandler> = handlers
}
