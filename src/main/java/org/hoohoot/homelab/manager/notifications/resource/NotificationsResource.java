package org.hoohoot.homelab.manager.notifications.resource;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.Record;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
public class NotificationsResource {
    private static final Logger LOGGER = Logger.getLogger(NotificationsResource.class);

    private final Emitter<Record<String, String>> notificationEmitter;

    public NotificationsResource(@Channel("incoming-notifications") Emitter<Record<String, String>> priceEmitter) {
        this.notificationEmitter = priceEmitter;
    }

    @POST
    @Path("/{source}")
    public Uni<Response> pushNotification(@RequestBody JsonObject notification, @PathParam("source") String source) {
        Record<String, String> record = Record.of(source, notification.toString());
        return Uni
                .createFrom().item(this.notificationEmitter.send(record))
                .onItem().transform(r -> Response.ok().build())
                .onFailure().invoke(throwable -> LOGGER.error("Failed to send notification to kafka", throwable))
                .onFailure().recoverWithItem(throwable -> Response.serverError().build());
    }
}

