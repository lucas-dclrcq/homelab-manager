package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.domain.ParseIssue
import org.hoohoot.homelab.manager.notifications.domain.toImdbLink

data class NotifyIssueEvent(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyIssueEventHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyIssueEvent> {
    override suspend fun handle(command: NotifyIssueEvent) {
        val issue = ParseIssue.from(command.webhookPayload)

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        this.notificationGateway.sendIssueNotification(notification)
    }
}