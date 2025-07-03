package org.hoohoot.homelab.manager.application.commands

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.notifications.NotificationGateway
import org.hoohoot.homelab.manager.domain.media_notifications.NotificationBuilder
import org.hoohoot.homelab.manager.domain.media_notifications.ParseMusic

data class NotifyAlbumDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyMusicDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyAlbumDownloaded> {
    override suspend fun handle(command: NotifyAlbumDownloaded) {
        val album = ParseMusic.from(command.webhookPayload)

        Log.info("Notifying album downloaded : ${album.albumTitle}")

        val notification = NotificationBuilder()
            .addTitle("Album downloaded")
            .addInfoLine("${album.artistName} - ${album.albumTitle} (${album.year})")
            .addInfoLine("Cover: ${album.coverUrl}")
            .addInfoLine("Genres : ${album.genres.joinToString(", ")}")
            .addInfoLine("Source : ${album.downloadClient}")
            .buildNotification()

        this.notificationGateway.sendMusicNotification(notification)
    }
}
