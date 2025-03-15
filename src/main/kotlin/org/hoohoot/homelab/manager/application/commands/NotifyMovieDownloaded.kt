package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.NotificationBuilder
import org.hoohoot.homelab.manager.domain.ParseMovie
import org.hoohoot.homelab.manager.domain.toImdbLink

data class NotifyMovieDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyMovieDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyMovieDownloaded> {
    override suspend fun handle(command: NotifyMovieDownloaded) {
        val movie = ParseMovie.from(command.webhookPayload)

        Log.info("Notifying movie downloaded : ${movie.title}")

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("Requested by : ${movie.requester}")
            .buildNotification()

        this.notificationGateway.sendMediaNotification(notification)
    }
}