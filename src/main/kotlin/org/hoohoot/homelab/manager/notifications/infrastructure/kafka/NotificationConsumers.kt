package org.hoohoot.homelab.manager.notifications.infrastructure.kafka

import com.trendyol.kediatr.Mediator
import io.quarkus.logging.Log
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.hoohoot.homelab.manager.notifications.application.usecases.*

class NotificationConsumers(private val mediator: Mediator) {
    @Incoming("radarr-notifications")
    suspend fun processRadarrNotifications(payload: JsonObject) = mediator.send(NotifyMovieDownloaded(payload))

    @Incoming("sonarr-notifications")
    suspend fun processSonarrNotifications(payload: JsonObject) = mediator.send(NotifySeriesDownloaded(payload))

    @Incoming("lidarr-notifications")
    suspend fun processLidarrNotifications(payload: JsonObject) = mediator.send(NotifyAlbumDownloaded(payload))

    @Incoming("jellyseerr-notifications")
    suspend fun processJellyseerrNotifications(payload: JsonObject) {
        return when (payload.getString("notification_type")) {
            "ISSUE_CREATED" -> mediator.send(NotifyIssueCreated(payload))
            "ISSUE_RESOLVED" -> mediator.send(NotifyIssueResolved(payload))
            "ISSUE_COMMENT" -> mediator.send(NotifyIssueCommented(payload))
            else -> Log.warn("Unhandled jellyseerr type: ${payload.getString("notification_type")}")
        }
    }
}
