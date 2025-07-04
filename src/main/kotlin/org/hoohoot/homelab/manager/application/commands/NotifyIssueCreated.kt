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

data class NotifyIssueCreated(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyIssueEventHandler(private val notificationGateway: NotificationGateway, private val notificationRepository: NotificationRepository) : CommandHandler<NotifyIssueCreated> {
    override suspend fun handle(command: NotifyIssueCreated) {
        val issue = ParseIssue.from(command.webhookPayload)

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

        val sentNotificationId = this.notificationGateway.sendSupportNotification(notification)

        this.notificationRepository.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }
}