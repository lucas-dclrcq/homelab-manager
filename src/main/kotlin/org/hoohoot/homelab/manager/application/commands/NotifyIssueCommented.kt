package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationRepository
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.ParseIssue

data class NotifyIssueCommented(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyIssueCommentedHandler(private val notificationGateway: NotificationGateway, private val issueRepository: NotificationRepository) : CommandHandler<NotifyIssueCommented> {
    override suspend fun handle(command: NotifyIssueCommented) {
        val issue = ParseIssue.from(command.webhookPayload)

        Log.info("Notifying issue commented : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Comment : ${issue.comment ?: "No comment"}")
            .addInfoLine("Comment by : ${issue.commentedBy ?: "No reporter"}")
            .buildNotification()

        val issueCreatedNotificationId = issueRepository.getNotificationIdForIssue(issue.id)

        if (issueCreatedNotificationId != null) {
            this.notificationGateway.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            this.notificationGateway.sendSupportNotification(notification)
        }
    }
}