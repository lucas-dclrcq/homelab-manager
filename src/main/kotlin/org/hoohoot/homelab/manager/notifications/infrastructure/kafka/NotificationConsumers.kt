package org.hoohoot.homelab.manager.notifications.infrastructure.kafka

import com.trendyol.kediatr.Mediator
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.hoohoot.homelab.manager.notifications.application.usecases.NotifyIssueEvent
import org.hoohoot.homelab.manager.notifications.application.usecases.NotifyMovieDownloaded
import org.hoohoot.homelab.manager.notifications.application.usecases.NotifyAlbumDownloaded
import org.hoohoot.homelab.manager.notifications.application.usecases.NotifySeriesDownloaded

class NotificationConsumers(private val mediator: Mediator) {
    @Incoming("radarr-notifications")
    suspend fun processRadarrNotifications(payload: JsonObject) = mediator.send(NotifyMovieDownloaded(payload))

    @Incoming("sonarr-notifications")
    suspend fun processSonarrNotifications(payload: JsonObject) = mediator.send(NotifySeriesDownloaded(payload))

    @Incoming("lidarr-notifications")
    suspend fun processLidarrNotifications(payload: JsonObject) = mediator.send(NotifyAlbumDownloaded(payload))

    @Incoming("jellyseerr-notifications")
    suspend fun processJellyseerrNotifications(payload: JsonObject) = mediator.send(NotifyIssueEvent(payload))
}
