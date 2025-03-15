package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationBus

data class PublishGenericNotification(val source: String, val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class PublishGenericNotificationHandler(private val notificationBus: NotificationBus) :
    CommandHandler<PublishGenericNotification> {
    override suspend fun handle(command: PublishGenericNotification) {
        Log.info("Publishing generic notification : ${command.source}")
        this.notificationBus.publishGenericNotification(command.source, command.webhookPayload)
    }
}