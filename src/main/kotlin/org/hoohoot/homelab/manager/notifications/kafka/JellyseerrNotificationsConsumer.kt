package org.hoohoot.homelab.manager.notifications.kafka

import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.matrix.MatrixAPI
import org.hoohoot.homelab.manager.notifications.matrix.MatrixMessage.Companion.html
import org.hoohoot.homelab.manager.notifications.matrix.MatrixRoomsConfiguration
import org.hoohoot.homelab.manager.notifications.parser.ParseIssue
import java.util.*

class JellyseerrNotificationsConsumer(@param:RestClient private val matrixAPI: MatrixAPI, private val matrixRooms: MatrixRoomsConfiguration) {

    @Incoming("jellyseerr-notifications")
    fun process(payload: JsonObject): Uni<Void> {

        val parseIssue = ParseIssue.from(payload)

        val notificationContent ="<h1>${parseIssue.title()}</h1><p>- Subject : ${parseIssue.subject()}<br>- Message : ${parseIssue.message()}<br>- Reporter : ${parseIssue.reportedByUserName()}</p>"

        return matrixAPI.sendMessage(
            matrixRooms.jellyseerr(),
            UUID.randomUUID().toString(),
            html(notificationContent)
        ).replaceWithVoid()
    }
}