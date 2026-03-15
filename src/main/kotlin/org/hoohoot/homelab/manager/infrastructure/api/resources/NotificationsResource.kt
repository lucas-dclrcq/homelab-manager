package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import io.quarkus.logging.Log
import io.vertx.core.json.JsonObject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.commands.*
import org.hoohoot.homelab.manager.domain.media_notifications.ParseIssue
import org.hoohoot.homelab.manager.domain.media_notifications.ParseMovie
import org.hoohoot.homelab.manager.domain.media_notifications.ParseMusic
import org.hoohoot.homelab.manager.infrastructure.api.dto.SonarrWebhookPayload

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationsResource(private val mediator: Mediator) {

    @POST
    @Path("/radarr")
    suspend fun handleRadarrNotification(payload: JsonObject): Response {
        if (payload.getString("eventType") != "Download") {
            Log.debug("Ignoring radarr event: ${payload.getString("eventType")}")
            return Response.noContent().build()
        }
        mediator.send(NotifyMovieDownloaded(ParseMovie.from(payload)))
        return Response.noContent().build()
    }

    @POST
    @Path("/sonarr")
    suspend fun handleSonarrNotification(payload: SonarrWebhookPayload): Response {
        mediator.send(NotifySeriesDownloaded(payload))
        return Response.noContent().build()
    }

    @POST
    @Path("/lidarr")
    suspend fun handleLidarrNotification(payload: JsonObject): Response {
        if (payload.getString("eventType") != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.getString("eventType")}")
            return Response.noContent().build()
        }
        mediator.send(NotifyAlbumDownloaded(ParseMusic.from(payload)))
        return Response.noContent().build()
    }

    @POST
    @Path("/jellyseerr")
    suspend fun handleJellyseerrNotification(payload: JsonObject): Response {
        mediator.send(NotifyJellyseerrEvent(ParseIssue.from(payload)))
        return Response.noContent().build()
    }

    @POST
    @Path("/send-whats-next-report")
    suspend fun sendWhatsNextReport() = mediator.send(SendWhatsNextWeeklyReport)
}
