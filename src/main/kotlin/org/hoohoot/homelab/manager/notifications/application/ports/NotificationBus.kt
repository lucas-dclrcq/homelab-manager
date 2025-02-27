package org.hoohoot.homelab.manager.notifications.application.ports

import io.vertx.core.json.JsonObject

interface NotificationBus {
    suspend fun publishGenericNotification(source: String, incomingNotificationPayload: JsonObject)
}