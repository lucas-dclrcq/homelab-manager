package org.hoohoot.homelab.manager.notifications.infrastructure.kafka

import io.quarkus.logging.Log
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.smallrye.mutiny.replaceWithUnit
import io.smallrye.reactive.messaging.kafka.Record
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationBus

@ApplicationScoped
class KafkaNotificationBus(@param:Channel("incoming-notifications") private val notificationEmitter: Emitter<Record<String, String>>) : NotificationBus {
    override suspend fun publishGenericNotification(source: String, incomingNotificationPayload: JsonObject) {
        val record = Record.of(source, incomingNotificationPayload.toString())
        return Uni.createFrom().item(notificationEmitter.send(record))
            .onItem().transform { Response.ok().build() }
            .onFailure().invoke { throwable -> Log.error("Failed to send notification to kafka", throwable) }
            .replaceWithUnit()
            .awaitSuspending()
    }
}