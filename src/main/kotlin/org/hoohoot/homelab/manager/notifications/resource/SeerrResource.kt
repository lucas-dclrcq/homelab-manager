package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.Issue
import org.hoohoot.homelab.manager.notifications.SeerrWebhookPayload
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@Path("/api/notifications/seerr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class SeerrResource(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleSeerrNotification(payload: SeerrWebhookPayload): Response {
        val issue = Issue.from(payload)

        when (issue.notificationType) {
            "ISSUE_CREATED" -> handleIssueCreated(issue)
            "ISSUE_RESOLVED" -> handleIssueResolved(issue)
            "ISSUE_REOPENED" -> handleIssueReopened(issue)
            "ISSUE_COMMENT" -> handleIssueComment(issue)
            else -> Log.warn("Unhandled seerr type: ${issue.notificationType}")
        }

        return Response.noContent().build()
    }

    private suspend fun handleIssueCreated(issue: Issue) {
        Log.info("Notifying issue created : ${issue.title}")

        val additionalInfo = if (issue.additionalInfo.isNotEmpty()) {
            val info = issue.additionalInfo.entries.joinToString("\n") { "- ${it.key} : ${it.value}" }
            "\nℹ️ Additional infos :\n$info"
        } else ""

        val additionalInfoHtml = if (issue.additionalInfo.isNotEmpty()) {
            val info = issue.additionalInfo.entries.joinToString("<br>") { "- ${it.key} : ${it.value}" }
            "<br>ℹ️ Additional infos :<br>$info"
        } else ""

        val content = RoomMessageEventContent.TextBased.Text(
            body = """
                🐛 ${issue.title}
                📌 Subject : ${issue.subject}
                💬 Message : ${issue.message}
                👤 Reporter : ${issue.reportedByUserName}
            """.trimIndent() + additionalInfo,
            format = "org.matrix.custom.html",
            formattedBody = "<h1>🐛 ${issue.title}</h1>" +
                "<p>📌 Subject : ${issue.subject}" +
                "<br>💬 Message : ${issue.message}" +
                "<br>👤 Reporter : ${issue.reportedByUserName}$additionalInfoHtml</p>"
        )

        val sentNotificationId = matrixSender.sendSupportNotification(content)
        notificationRepo.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }

    private suspend fun handleIssueResolved(issue: Issue) {
        Log.info("Notifying issue resolved : ${issue.title}")

        val content = issueContent("✅", issue)
        sendSupportNotificationInThread(content, issue.id)

        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issue.id)
        if (issueCreatedNotificationId != null) {
            matrixSender.reactToSupportMessage(issueCreatedNotificationId, "✅")
        }
    }

    private suspend fun handleIssueReopened(issue: Issue) {
        Log.info("Notifying issue reopened : ${issue.title}")
        sendSupportNotificationInThread(issueContent("🔄", issue), issue.id)
    }

    private suspend fun handleIssueComment(issue: Issue) {
        Log.info("Notifying issue commented : ${issue.title}")

        val content = RoomMessageEventContent.TextBased.Text(
            body = """
                💬 ${issue.title}
                📌 Subject : ${issue.subject}
                💬 Comment : ${issue.comment ?: "No comment"}
                👤 Comment by : ${issue.commentedBy ?: "No reporter"}
            """.trimIndent(),
            format = "org.matrix.custom.html",
            formattedBody = "<h1>💬 ${issue.title}</h1>" +
                "<p>📌 Subject : ${issue.subject}" +
                "<br>💬 Comment : ${issue.comment ?: "No comment"}" +
                "<br>👤 Comment by : ${issue.commentedBy ?: "No reporter"}</p>"
        )

        sendSupportNotificationInThread(content, issue.id)
    }

    private fun issueContent(emoji: String, issue: Issue): RoomMessageEventContent.TextBased.Text {
        return RoomMessageEventContent.TextBased.Text(
            body = """
                $emoji ${issue.title}
                📌 Subject : ${issue.subject}
                💬 Message : ${issue.message}
                👤 Reporter : ${issue.reportedByUserName}
            """.trimIndent(),
            format = "org.matrix.custom.html",
            formattedBody = "<h1>$emoji ${issue.title}</h1>" +
                "<p>📌 Subject : ${issue.subject}" +
                "<br>💬 Message : ${issue.message}" +
                "<br>👤 Reporter : ${issue.reportedByUserName}</p>"
        )
    }

    private suspend fun sendSupportNotificationInThread(content: RoomMessageEventContent.TextBased.Text, issueId: String) {
        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issueId)

        if (issueCreatedNotificationId != null) {
            matrixSender.sendSupportNotification(content, issueCreatedNotificationId)
        } else {
            matrixSender.sendSupportNotification(content)
        }
    }
}
