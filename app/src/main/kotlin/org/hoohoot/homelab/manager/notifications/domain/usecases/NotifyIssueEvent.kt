package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationMessage
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationThreads

data class IssueReport(
    val issueId: String,
    val title: String,
    val subject: String,
    val message: String,
    val reporter: String,
    val additionalInfo: Map<String, String> = emptyMap(),
    val commentMessage: String? = null,
    val commentedBy: String? = null,
)

@ApplicationScoped
class NotifyIssueEvent(
    private val sender: NotificationSender,
    private val threads: NotificationThreads,
) {
    suspend fun created(issue: IssueReport) {
        val additionalLines = if (issue.additionalInfo.isNotEmpty()) {
            listOf("ℹ️ Additional infos :") + issue.additionalInfo.entries.map { "- ${it.key} : ${it.value}" }
        } else {
            emptyList()
        }
        val message = notificationMessage(
            "🐛 ${issue.title}",
            listOf(
                "📌 Subject : ${issue.subject}",
                "💬 Message : ${issue.message}",
                "👤 Reporter : ${issue.reporter}",
            ) + additionalLines,
        )
        val sentNotificationId = sender.send(NotificationRoom.SUPPORT, message)
        threads.saveNotificationIdForIssue(issue.issueId, sentNotificationId)
    }

    suspend fun resolved(issue: IssueReport) {
        sendInIssueThread(issueMessage("✅", issue), issue.issueId)

        val issueCreatedNotificationId = threads.getNotificationIdForIssue(issue.issueId)
        if (issueCreatedNotificationId != null) {
            sender.react(NotificationRoom.SUPPORT, issueCreatedNotificationId, "✅")
        }
    }

    suspend fun reopened(issue: IssueReport) {
        sendInIssueThread(issueMessage("🔄", issue), issue.issueId)
    }

    suspend fun commented(issue: IssueReport) {
        val message = notificationMessage(
            "💬 ${issue.title}",
            listOf(
                "📌 Subject : ${issue.subject}",
                "💬 Comment : ${issue.commentMessage ?: "No comment"}",
                "👤 Comment by : ${issue.commentedBy ?: "No reporter"}",
            ),
        )
        sendInIssueThread(message, issue.issueId)
    }

    private fun issueMessage(emoji: String, issue: IssueReport) = notificationMessage(
        "$emoji ${issue.title}",
        listOf(
            "📌 Subject : ${issue.subject}",
            "💬 Message : ${issue.message}",
            "👤 Reporter : ${issue.reporter}",
        ),
    )

    private suspend fun sendInIssueThread(message: NotificationMessage, issueId: String) {
        val issueCreatedNotificationId = threads.getNotificationIdForIssue(issueId)
        sender.send(NotificationRoom.SUPPORT, message, issueCreatedNotificationId)
    }
}
