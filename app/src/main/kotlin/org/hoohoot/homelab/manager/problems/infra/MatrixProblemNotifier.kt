package org.hoohoot.homelab.manager.problems.infra

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationId
import org.hoohoot.homelab.manager.notifications.domain.NotificationMessage
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemNotifier
import org.slf4j.LoggerFactory

@ApplicationScoped
class MatrixProblemNotifier(
    private val sender: NotificationSender,
) : ProblemNotifier {
    private val log = LoggerFactory.getLogger(MatrixProblemNotifier::class.java)

    override suspend fun workflowCreated(username: String, mediaType: String): NotificationId? =
        try {
            val mediaLabel = when (mediaType) {
                "movie" -> "film"
                "tv" -> "série"
                else -> mediaType
            }
            sender.send(
                room = NotificationRoom.SUPPORT,
                message = notificationMessage(
                    "🔧 Nouveau problème",
                    listOf("@$username commence un signalement ($mediaLabel)"),
                ),
            )
        } catch (e: Exception) {
            log.warn("Échec de l'envoi de la notification de création de workflow", e)
            null
        }

    override suspend fun problemReported(
        mediaTitle: String?,
        problemType: String?,
        description: String?,
        reporter: String,
        inThread: NotificationId?,
    ) {
        try {
            val problemLabel = when (problemType) {
                "vo_should_be_french" -> "VO au lieu de VF"
                "other" -> "Autre : ${description ?: "?"}"
                else -> problemType ?: "?"
            }
            val lines = mutableListOf(
                "📌 Média : ${mediaTitle ?: "?"}",
                "👤 Signalé par : @$reporter",
                "💬 $problemLabel",
            )
            sender.send(
                room = NotificationRoom.SUPPORT,
                message = notificationMessage("🐛 Problème signalé", lines),
                inThread = inThread,
            )
        } catch (e: Exception) {
            log.warn("Échec de l'envoi de la notification de signalement", e)
        }
    }

    override suspend fun problemResolved(
        mediaTitle: String?,
        reporter: String,
        resolvedBy: String,
        isSelfResolve: Boolean,
        inThread: NotificationId?,
    ) {
        try {
            val message = if (isSelfResolve) {
                notificationMessage(
                    "🙄 Problème résolu",
                    listOf("@$reporter a résolu son propre problème sur « ${mediaTitle ?: "?"} »... fallait juste chercher un peu 😏"),
                )
            } else {
                notificationMessage(
                    "✅ Problème résolu",
                    listOf("Problème sur « ${mediaTitle ?: "?"} » signalé par @$reporter, résolu par @$resolvedBy"),
                )
            }
            sender.send(
                room = NotificationRoom.SUPPORT,
                message = message,
                inThread = inThread,
            )
            if (inThread != null) {
                sender.react(NotificationRoom.SUPPORT, inThread, "✅")
            }
        } catch (e: Exception) {
            log.warn("Échec de l'envoi de la notification de résolution", e)
        }
    }
}
