package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.Issue
import org.hoohoot.homelab.manager.notifications.Notification
import org.hoohoot.homelab.manager.notifications.NotificationBuilder
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

        var notificationBuilder = NotificationBuilder()
            .addTitle("🐛 ${issue.title}")
            .addInfoLine("📌 Subject : ${issue.subject}")
            .addInfoLine("💬 Message : ${issue.message}")
            .addInfoLine("👤 Reporter : ${issue.reportedByUserName}")

        if (issue.additionalInfo.isNotEmpty()) {
            notificationBuilder = notificationBuilder
                .addInfoLine("ℹ️ Additional infos :")
                .addInfoLines(issue.additionalInfo.map { "- ${it.key} : ${it.value}" })
        }

        val notification = notificationBuilder.buildNotification()
        val sentNotificationId = matrixSender.sendSupportNotification(notification)
        notificationRepo.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }

    private suspend fun handleIssueResolved(issue: Issue) {
        Log.info("Notifying issue resolved : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle("✅ ${issue.title}")
            .addInfoLine("📌 Subject : ${issue.subject}")
            .addInfoLine("💬 Message : ${issue.message}")
            .addInfoLine("👤 Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        sendSupportNotificationInThread(notification, issue.id)

        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issue.id)
        if (issueCreatedNotificationId != null) {
            matrixSender.reactToSupportMessage(issueCreatedNotificationId, "✅")
        }
    }

    private suspend fun handleIssueReopened(issue: Issue) {
        Log.info("Notifying issue reopened : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle("🔄 ${issue.title}")
            .addInfoLine("📌 Subject : ${issue.subject}")
            .addInfoLine("💬 Message : ${issue.message}")
            .addInfoLine("👤 Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        sendSupportNotificationInThread(notification, issue.id)
    }

    private suspend fun handleIssueComment(issue: Issue) {
        Log.info("Notifying issue commented : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle("💬 ${issue.title}")
            .addInfoLine("📌 Subject : ${issue.subject}")
            .addInfoLine("💬 Comment : ${issue.comment ?: "No comment"}")
            .addInfoLine("👤 Comment by : ${issue.commentedBy ?: "No reporter"}")
            .buildNotification()

        sendSupportNotificationInThread(notification, issue.id)
    }

    private suspend fun sendSupportNotificationInThread(notification: Notification, issueId: String) {
        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issueId)

        if (issueCreatedNotificationId != null) {
            matrixSender.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            matrixSender.sendSupportNotification(notification)
        }
    }
}
