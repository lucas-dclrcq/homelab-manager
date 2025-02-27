package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.IssueRepository
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.domain.ParseIssue

data class NotifyIssueCreated(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyIssueEventHandler(private val notificationGateway: NotificationGateway, private val issueRepository: IssueRepository) : CommandHandler<NotifyIssueCreated> {
    override suspend fun handle(command: NotifyIssueCreated) {
        val issue = ParseIssue.from(command.webhookPayload)

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        val sentNotificationId = this.notificationGateway.sendSupportNotification(notification)

        this.issueRepository.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }
}