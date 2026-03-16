package org.hoohoot.homelab.manager.notifications.matrix.bot.commands.regex

import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.notifications.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.notifications.matrix.bot.commands.RegexBotCommand
import kotlin.random.Random

@ApplicationScoped
class DeadooMatrixCommand : RegexBotCommand() {
    override val name: String = "deadoo"
    override val help: String = ""
    override val autoAcknowledge = false
    override val regex: Regex =
        Regex(".*(c'est comment|d+e+a+d+o+).*", RegexOption.IGNORE_CASE)

    private val deadosWithWeights = listOf(
        "DEAAAADOOOO" to 100,
        "Dddddeeeeeaaaddoooo" to 100,
        "Deado" to 100,
        "deaaaaado" to 100,
        "C'est deaaaaaado" to 100,
        "C'est deado" to 100,
        "IT IS DEEEEAADO" to 70,
        "DEADO !!!" to 70,
        "C'est complètement deado" to 70,
        "Totalement deado" to 70,
        "deado deado deado" to 70,
        "D-E-A-D-O" to 50,
        "Deado à mort" to 50,
        "Super deado" to 50,
        "Méga deado" to 50,
        "Ultra deado" to 50,
        "Deado forever" to 20,
        "RIP deado" to 20,
        "Archi deado" to 20,
        "Deado 💀" to 10,
        "Deado dans l'âme" to 10,
        "DEEEP DEEADOO" to 10,
        "FIYAH DEADO 🔥🔥🔥" to 1,
        "DEADO INNA CHEMINAY" to 1,
        "DEADOCLAAT ❤️💛💚" to 1,
        "BRAINDEADO" to 1,
        "DEADOSAMAAAA (˶˃ ᵕ ˂˶)💞" to 1,
        "ÉLÉMENTAIREMENT DEADO" to 1
    )

    private val totalWeight = deadosWithWeights.sumOf { it.second }

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        if (!matrixBot.isSameUser(sender)) {
            val randomValue = Random.nextInt(1, totalWeight + 1)
            var currentWeight = 0

            var deado = deadosWithWeights.first().first
            for ((d, weight) in deadosWithWeights) {
                currentWeight += weight
                if (randomValue <= currentWeight) {
                    deado = d
                    break
                }
            }

            matrixBot.room().sendMessage(roomId) { text(deado) }
        }
    }
}
