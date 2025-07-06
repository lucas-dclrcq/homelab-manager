package org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.regex

import com.trendyol.kediatr.Mediator
import jakarta.enterprise.context.ApplicationScoped
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.EventId
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.application.queries.CestDeado
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.MatrixBot
import org.hoohoot.homelab.manager.infrastructure.matrix.bot.commands.RegexBotCommand

@ApplicationScoped
class DeadooMatrixCommand(private val mediator: Mediator) : RegexBotCommand() {
    override val name: String = "deadoo"
    override val help: String = ""
    override val autoAcknowledge = false
    override val regex: Regex =
        """.*(c'est comment|d+e+a+d+o+|défunt|cadavre|résurrection|tombeau|cercueil|agonie|euthanasie|dépouille|deuil|décès|meurtre|suicide|pendu|ressusciter|posthume|exécution|tombe|funèbre|cimetière|disparu|supplice|immortalité|mourir|vivant|assassinat|enterrement|faucheuse|autopsie|mortel|mortuaire|noyé|trépas|enfer|funéraire|blessé|perte|strangulation|survivant|crémation|éternité|glas|pendaison|tué|claqué|noyade|destruction|immortel|catastrophe|disparition|meurtrier|spiritisme|tragique|trépassé|assassin|hadès|paradis|repos|succession|vieillesse|corbillard|dernier souffle|extrême-onction|funérailles|funeste|mourant|bourreau|fossoyeur|inanimé|macchabée|thanatos|crevé|désolation|sépulture|thanatologie|enterré|épitaphe|porté disparu|spectre|taxidermie|embaumement|linceul|martyr|martyre|matador|métempsycose|nécromancie|requiem|vampire|châtiment|coroner|dernier soupir|guillotine|mémoire|néant|poison|sauver|suicider|apoptose|évanoui|infarctus du myocarde|macabre|nécrose|survie|torture|assassiné|bûcher|canné|désert|échafaud|fatigué|foutu|naze|sépulcre|chagrin|charogne|condamné|crime|empoisonnement|mort-né|proscription|puni|régicide|six pieds sous terre|victime|cénotaphe|cinéraire|décédé|ensevelissement|gisant|limbe|maladie|messe|mort|morte|mortalité|nuit|peine capitale|pendre|venger|fosse|gibet|parque|pécheur|sommeil|supplicié|testament|charnier|commémoration|danger|ensevelir|heure suprême|prématurée|purgatoire|seuil|veuve|décapité|étranglé|vie éternelle|asphyxie|charon|exécuter|inerte|post-mortem|ruine|achever|anéantissement|croque-mort|crucifié|crypte|éteint|exténué|extinction|fantôme|morgue|mors|nécrologie|odin|péché|pourriture|vallée de la mort|attentat|autre monde|champ de bataille|crématoire|décapitation|dernier adieu|déterrer|empoisonné|génocide|heure dernière|inhumé|intestat|nécromasse|nécrophile|outre-tombe|psychopompe|regret|repos éternel|reste|sarcophage|suaire|survivance|valkyrie|viatique|accidentelle|agonisant|descente aux enfers|douloureuse|épuisé|fin|frôlé|fusillade|instantanée|lente|létal|monument|mortifère|naissance|nécrophilie|oraison funèbre|succéder|suppression|accident de la route|atroces|condamnation|décimer|délivrera|élégie|épouvante|éternelle|honorer|incinération|inhumation|jonchée|menace|mort subite|naufrage|obsèques|ossements|peine|pleurer|présage|souffrance|surdose|survenue|terrible|vaincu|annoncée|anticipée|au-delà|blessure|coma|condamnent|cruel|écroulement|homicide|horrible|ignominie|messager|parricide|sauveur|tuant|vengeance|ankylosé|apathique|bienheureux|bousillé|brisé|cassé|chute|conclusion|décomposition|défunction|délavé|dénouement).*""".toRegex(
            RegexOption.IGNORE_CASE
        )

    override suspend fun handle(
        matrixBot: MatrixBot,
        sender: UserId,
        roomId: RoomId,
        parameters: String,
        textEventId: EventId,
        textEvent: RoomMessageEventContent.TextBased.Text
    ) {
        if (!matrixBot.isSameUser(sender)) {
            val deeeeaaadddoooo = mediator.send(CestDeado)
            matrixBot.room().sendMessage(roomId) { text(deeeeaaadddoooo) }
        }
    }
}