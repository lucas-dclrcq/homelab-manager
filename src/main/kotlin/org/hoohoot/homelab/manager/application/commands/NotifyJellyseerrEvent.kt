package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationRepository
import org.hoohoot.homelab.manager.domain.media_notifications.Issue
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder

data class NotifyJellyseerrEvent(val issue: Issue) : Command

@Startup
@ApplicationScoped
class NotifyJellyseerrEventHandler(
    private val notificationGateway: NotificationGateway,
    private val notificationRepository: NotificationRepository
) : CommandHandler<NotifyJellyseerrEvent> {

    override suspend fun handle(command: NotifyJellyseerrEvent) {
        val issue = command.issue

        when (issue.notificationType) {
            "ISSUE_CREATED" -> handleIssueCreated(issue)
            "ISSUE_RESOLVED" -> handleIssueReplyNotification(issue)
            "ISSUE_COMMENT" -> handleIssueCommentNotification(issue)
            else -> Log.warn("Unhandled jellyseerr type: ${issue.notificationType}")
        }
    }

    private suspend fun handleIssueCreated(issue: Issue) {
        Log.info("Notifying issue created : ${issue.title}")

        var notificationBuilder = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")

        if (issue.additionalInfo.isNotEmpty()) {
            notificationBuilder = notificationBuilder
                .addInfoLine("Additional infos :")
                .addInfoLines(issue.additionalInfo.map { "- ${it.key} : ${it.value}" })
        }

        val notification = notificationBuilder.buildNotification()
        val sentNotificationId = notificationGateway.sendSupportNotification(notification)
        notificationRepository.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }

    private suspend fun handleIssueReplyNotification(issue: Issue) {
        Log.info("Notifying issue resolved : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        val issueCreatedNotificationId = notificationRepository.getNotificationIdForIssue(issue.id)

        if (issueCreatedNotificationId != null) {
            notificationGateway.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            notificationGateway.sendSupportNotification(notification)
        }
    }

    private suspend fun handleIssueCommentNotification(issue: Issue) {
        Log.info("Notifying issue commented : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Comment : ${issue.comment ?: "No comment"}")
            .addInfoLine("Comment by : ${issue.commentedBy ?: "No reporter"}")
            .buildNotification()

        val issueCreatedNotificationId = notificationRepository.getNotificationIdForIssue(issue.id)

        if (issueCreatedNotificationId != null) {
            notificationGateway.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            notificationGateway.sendSupportNotification(notification)
        }
    }
}
