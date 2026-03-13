package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.Movie
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.toImdbLink

data class NotifyMovieDownloaded(val movie: Movie) : Command

@Startup
@ApplicationScoped
class NotifyMovieDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyMovieDownloaded> {
    override suspend fun handle(command: NotifyMovieDownloaded) {
        val movie = command.movie

        Log.info("Notifying movie downloaded : ${movie.title}")

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("Requested by : ${movie.requester}")
            .buildNotification()

        notificationGateway.sendMediaNotification(notification)
    }
}
