package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.domain.ParseMovie
import org.hoohoot.homelab.manager.notifications.domain.toImdbLink

data class NotifyMovieDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyMovieDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyMovieDownloaded> {
    override suspend fun handle(command: NotifyMovieDownloaded) {
        val movie = ParseMovie.from(command.webhookPayload)

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("Requested by : ${movie.requester}")
            .buildNotification()

        this.notificationGateway.sendMediaNotification(notification)
    }
}