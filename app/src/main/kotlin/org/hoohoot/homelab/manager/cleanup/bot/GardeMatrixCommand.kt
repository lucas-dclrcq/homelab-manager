package org.hoohoot.homelab.manager.cleanup.bot

import de.connect2x.trixnity.client.room.message.react
import de.connect2x.trixnity.client.room.message.text
import de.connect2x.trixnity.core.model.EventId
import de.connect2x.trixnity.core.model.RoomId
import de.connect2x.trixnity.core.model.UserId
import de.connect2x.trixnity.core.model.events.m.room.RoomMessageEventContent
import io.quarkus.logging.Log
import io.vertx.core.Vertx
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.VetoByTitleResult
import org.hoohoot.homelab.manager.cleanup.domain.usecases.VetoByTitle
import org.hoohoot.homelab.manager.shared.matrix.bot.MatrixBotSession
import org.hoohoot.homelab.manager.shared.matrix.bot.commands.PrefixedBotCommand
import org.hoohoot.homelab.manager.shared.vertx.runOnSafeVertxContext

@ApplicationScoped
class GardeMatrixCommand(
    private val vetoByTitle: VetoByTitle,
    private val vertx: Vertx,
) : PrefixedBotCommand() {
    override val name: String = "garde"
    override val params: String = "<titre>"
    override val help: String =
        "Protège un média de la campagne de nettoyage en cours (usage : !johnny garde <titre>)"
    override val autoAcknowledge = false

    override suspend fun handle(
        session: MatrixBotSession,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text,
    ) {
        if (parameters.isBlank()) {
            session.room.sendMessage(roomId) { text("Dis-moi quoi garder : !johnny garde <titre>") }
            return
        }

        Log.info("Cleanup: veto requested by ${sender.localpart} for '$parameters'")

        // Les commandes bot tournent sur les dispatchers trixnity : Panache exige un contexte Vertx safe
        val result = runOnSafeVertxContext(vertx) { vetoByTitle(parameters, sender.localpart) }

        when (result) {
            is VetoByTitleResult.Ok -> {
                session.room.sendMessage(roomId) { react(textEventId, ACK_EMOJI) }
                val kept = result.protectedTitles.joinToString(", ")
                session.room.sendMessage(roomId) { text("C'est noté, on garde : $kept 🛡️") }
            }
            VetoByTitleResult.NoCampaign ->
                session.room.sendMessage(roomId) { text("Aucune campagne de nettoyage en cours, rien à garder !") }
            is VetoByTitleResult.NoMatch -> {
                val hint = if (result.proposedTitles.isEmpty()) {
                    "plus aucun candidat en attente."
                } else {
                    "candidats : ${result.proposedTitles.joinToString(", ")}"
                }
                session.room.sendMessage(roomId) { text("Aucun candidat ne correspond à « $parameters ». $hint") }
            }
            is VetoByTitleResult.Ambiguous ->
                session.room.sendMessage(roomId) {
                    text("Plusieurs médias correspondent : ${result.titles.joinToString(", ")}. Sois plus précis !")
                }
        }
    }
}
