package org.hoohoot.homelab.manager.notifications.infrastructure.api

import com.trendyol.kediatr.Mediator
import io.vertx.core.json.JsonObject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.application.usecases.PublishGenericNotification
import org.hoohoot.homelab.manager.notifications.application.usecases.SendWhatsNextWeeklyReport

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationsResource(private val mediator: Mediator) {
    @POST
    @Path("/incoming/{source}")
    suspend fun handleIncomingNotification(@RequestBody notification: JsonObject, @PathParam("source") source: String) =
        this.mediator.send(PublishGenericNotification(source, notification))

    @POST
    @Path("/send-whats-next-report")
    suspend fun sendWhatsNextReport() = this.mediator.send(SendWhatsNextWeeklyReport)
}