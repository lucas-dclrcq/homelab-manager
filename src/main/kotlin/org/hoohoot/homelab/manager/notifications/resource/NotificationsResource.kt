package org.hoohoot.homelab.manager.notifications.resource

import io.smallrye.mutiny.Uni
import io.smallrye.reactive.messaging.kafka.Record
import jakarta.json.JsonObject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.reactive.messaging.Channel
import org.eclipse.microprofile.reactive.messaging.Emitter
import org.jboss.logging.Logger

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationsResource(@param:Channel("incoming-notifications") private val notificationEmitter: Emitter<Record<String, String>>) {
    companion object {
        private val LOGGER: Logger = Logger.getLogger(NotificationsResource::class.java)
    }

    @POST
    @Path("/{source}")
    fun pushNotification(@RequestBody notification: JsonObject, @PathParam("source") source: String): Uni<Response> {
        val record = Record.of(source, notification.toString())
        return Uni
            .createFrom().item(notificationEmitter.send(record))
            .onItem().transform { Response.ok().build() }
            .onFailure().invoke { throwable -> LOGGER.error("Failed to send notification to kafka", throwable) }
            .onFailure().recoverWithItem { _: Throwable? -> Response.serverError().build() }
    }
}

