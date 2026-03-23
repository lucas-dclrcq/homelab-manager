package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.SeerrWebhookPayload
import org.hoohoot.homelab.manager.notifications.additionalInfo
import org.hoohoot.homelab.manager.notifications.commentMessage
import org.hoohoot.homelab.manager.notifications.commentedBy
import org.hoohoot.homelab.manager.notifications.issueId
import org.hoohoot.homelab.manager.notifications.matrix.MatrixConfiguration
import org.hoohoot.homelab.manager.notifications.matrix.sendNotification
import org.hoohoot.homelab.manager.notifications.matrix.sendReaction
import org.hoohoot.homelab.manager.notifications.message
import org.hoohoot.homelab.manager.notifications.notificationType
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository
import org.hoohoot.homelab.manager.notifications.reportedByUserName
import org.hoohoot.homelab.manager.notifications.subject
import org.hoohoot.homelab.manager.notifications.title

@Path("/api/notifications/seerr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class SeerrResource(
    private val matrixClient: MatrixClientServerApiClient,
    private val matrixConfig: MatrixConfiguration,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleSeerrNotification(payload: SeerrWebhookPayload): Response {
        when (payload.notificationType()) {
            "ISSUE_CREATED" -> handleIssueCreated(payload)
            "ISSUE_RESOLVED" -> handleIssueResolved(payload)
            "ISSUE_REOPENED" -> handleIssueReopened(payload)
            "ISSUE_COMMENT" -> handleIssueComment(payload)
            else -> Log.warn("Unhandled seerr type: ${payload.notificationType()}")
        }

        return Response.noContent().build()
    }

    private suspend fun handleIssueCreated(payload: SeerrWebhookPayload) {
        Log.info("Notifying issue created : ${payload.title()}")

        val additionalInfo = payload.additionalInfo()
        val additionalInfoText = if (additionalInfo.isNotEmpty()) {
            val info = additionalInfo.entries.joinToString("\n") { "- ${it.key} : ${it.value}" }
            "\nℹ️ Additional infos :\n$info"
        } else ""

        val additionalInfoHtml = if (additionalInfo.isNotEmpty()) {
            val info = additionalInfo.entries.joinToString("<br>") { "- ${it.key} : ${it.value}" }
            "<br>ℹ️ Additional infos :<br>$info"
        } else ""

        val content = RoomMessageEventContent.TextBased.Text(
            body = """
                🐛 ${payload.title()}
                📌 Subject : ${payload.subject()}
                💬 Message : ${payload.message()}
                👤 Reporter : ${payload.reportedByUserName()}
            """.trimIndent() + additionalInfoText,
            format = "org.matrix.custom.html",
            formattedBody = "<h1>🐛 ${payload.title()}</h1>" +
                "<p>📌 Subject : ${payload.subject()}" +
                "<br>💬 Message : ${payload.message()}" +
                "<br>👤 Reporter : ${payload.reportedByUserName()}$additionalInfoHtml</p>"
        )

        val supportRoom = matrixConfig.room().support()
        val sentNotificationId = matrixClient.sendNotification(content, supportRoom)
        notificationRepo.saveNotificationIdForIssue(payload.issueId(), sentNotificationId)
    }

    private suspend fun handleIssueResolved(payload: SeerrWebhookPayload) {
        Log.info("Notifying issue resolved : ${payload.title()}")

        val content = issueContent("✅", payload)
        sendSupportNotificationInThread(content, payload.issueId())

        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(payload.issueId())
        if (issueCreatedNotificationId != null) {
            matrixClient.sendReaction(issueCreatedNotificationId, matrixConfig.room().support(), "✅")
        }
    }

    private suspend fun handleIssueReopened(payload: SeerrWebhookPayload) {
        Log.info("Notifying issue reopened : ${payload.title()}")
        sendSupportNotificationInThread(issueContent("🔄", payload), payload.issueId())
    }

    private suspend fun handleIssueComment(payload: SeerrWebhookPayload) {
        Log.info("Notifying issue commented : ${payload.title()}")

        val content = RoomMessageEventContent.TextBased.Text(
            body = """
                💬 ${payload.title()}
                📌 Subject : ${payload.subject()}
                💬 Comment : ${payload.commentMessage() ?: "No comment"}
                👤 Comment by : ${payload.commentedBy() ?: "No reporter"}
            """.trimIndent(),
            format = "org.matrix.custom.html",
            formattedBody = "<h1>💬 ${payload.title()}</h1>" +
                "<p>📌 Subject : ${payload.subject()}" +
                "<br>💬 Comment : ${payload.commentMessage() ?: "No comment"}" +
                "<br>👤 Comment by : ${payload.commentedBy() ?: "No reporter"}</p>"
        )

        sendSupportNotificationInThread(content, payload.issueId())
    }

    private fun issueContent(emoji: String, payload: SeerrWebhookPayload): RoomMessageEventContent.TextBased.Text {
        return RoomMessageEventContent.TextBased.Text(
            body = """
                $emoji ${payload.title()}
                📌 Subject : ${payload.subject()}
                💬 Message : ${payload.message()}
                👤 Reporter : ${payload.reportedByUserName()}
            """.trimIndent(),
            format = "org.matrix.custom.html",
            formattedBody = "<h1>$emoji ${payload.title()}</h1>" +
                "<p>📌 Subject : ${payload.subject()}" +
                "<br>💬 Message : ${payload.message()}" +
                "<br>👤 Reporter : ${payload.reportedByUserName()}</p>"
        )
    }

    private suspend fun sendSupportNotificationInThread(content: RoomMessageEventContent.TextBased.Text, issueId: String) {
        val supportRoom = matrixConfig.room().support()
        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issueId)
        matrixClient.sendNotification(content, supportRoom, issueCreatedNotificationId)
    }
}
