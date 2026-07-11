package org.hoohoot.homelab.manager.cleanup.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.domain.ExecutionSummary
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupNotifier
import org.hoohoot.homelab.manager.notifications.domain.NotificationMessage
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import java.time.format.DateTimeFormatter

@ApplicationScoped
class MatrixCleanupNotifier(
    private val sender: NotificationSender,
    @param:ConfigProperty(name = "app.public-url") private val publicUrl: String,
) : CleanupNotifier {
    companion object {
        private const val MAX_LISTED_CANDIDATES = 15
        private val DEADLINE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    }

    override suspend fun announceCampaign(
        campaign: CleanupCampaignEntity,
        candidates: List<CleanupCandidateEntity>,
    ): String? {
        val deadline = campaign.graceEndsAt.format(DEADLINE_FORMAT)
        val listed = candidates.take(MAX_LISTED_CANDIDATES)
        val remaining = candidates.size - listed.size

        val candidateLines = listed.map {
            val icon = if (it.mediaKind == CleanupCandidateEntity.KIND_MOVIE) "🎬" else "📺"
            "$icon ${it.displayTitle()} — ${formatGb(it.sizeBytes)} — score ${it.score.toInt()}"
        } + if (remaining > 0) listOf("… et $remaining autres") else emptyList()

        val intro =
            "Il reste ${formatGb(campaign.freeBytesAtStart)} libres (seuil ${formatGb(campaign.thresholdBytes)}). " +
                "Sans veto d'ici le $deadline, ces médias seront supprimés " +
                "(${formatGb(campaign.targetBytesToFree)} à libérer) :"
        val howTo = "Pour garder un média : !johnny garde <titre> ou $publicUrl/cleanup"

        val header = "🧹 Grand ménage !"
        val body = (listOf(header, intro) + candidateLines + howTo).joinToString("\n")
        val formattedBody =
            "<h1>$header</h1><p>$intro</p><ul>" +
                candidateLines.joinToString("") { "<li>$it</li>" } +
                "</ul><p>Pour garder un média : <code>!johnny garde &lt;titre&gt;</code> " +
                "ou <a href=\"$publicUrl/cleanup\">$publicUrl/cleanup</a></p>"

        return sender.send(NotificationRoom.MEDIA, NotificationMessage(body, formattedBody)).value
    }

    override suspend fun announceExecution(campaign: CleanupCampaignEntity, summary: ExecutionSummary) {
        val lines = buildList {
            add("${summary.deletedCount} supprimés (${formatGb(summary.freedBytes)} libérés)")
            add("${summary.protectedCount} protégés par veto")
            if (summary.skippedCount > 0) add("${summary.skippedCount} ignorés")
            if (summary.failedCount > 0) add("⚠️ ${summary.failedCount} échecs (voir la page admin)")
            summary.note?.let { add(it) }
        }
        sender.send(NotificationRoom.MEDIA, message("🧹 Ménage terminé", lines))
    }

    override suspend fun announceCancellation(campaign: CleanupCampaignEntity) {
        sender.send(
            NotificationRoom.MEDIA,
            message("🧹 Campagne de nettoyage annulée", listOf("Rien ne sera supprimé.")),
        )
    }

    private fun message(header: String, lines: List<String>) = NotificationMessage(
        body = (listOf(header) + lines).joinToString("\n"),
        formattedBody = "<h1>$header</h1><p>${lines.joinToString("<br>")}</p>",
    )

    private fun formatGb(bytes: Long): String = "%.1f Go".format(bytes / 1e9)
}
