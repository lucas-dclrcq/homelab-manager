package org.hoohoot.homelab.manager.application.ports.notifications

import io.vertx.core.json.JsonObject

interface NotificationBus {
    suspend fun publishGenericNotification(source: String, incomingNotificationPayload: JsonObject)
}