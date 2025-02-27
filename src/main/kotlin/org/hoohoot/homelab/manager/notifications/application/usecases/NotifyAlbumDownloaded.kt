package org.hoohoot.homelab.manager.notifications.application.usecases

import com.trendyol.kediatr.Command
import com.trendyol.kediatr.CommandHandler
import io.quarkus.runtime.Startup
import io.vertx.core.json.JsonObject
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.NotificationGateway
import org.hoohoot.homelab.manager.notifications.domain.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.domain.ParseMusic

data class NotifyAlbumDownloaded(val webhookPayload: JsonObject) : Command

@Startup
@ApplicationScoped
class NotifyMusicDownloadedHandler(private val notificationGateway: NotificationGateway) : CommandHandler<NotifyAlbumDownloaded> {
    override suspend fun handle(command: NotifyAlbumDownloaded) {
        val album = ParseMusic.from(command.webhookPayload)

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${album.artistName} - ${album.albumTitle} (${album.year})")
            .addInfoLine("Cover: ${album.coverUrl}")
            .addInfoLine("Genres : ${album.genres.joinToString(", ")}")
            .addInfoLine("Source : ${album.downloadClient}")
            .buildNotification()

        this.notificationGateway.sendMusicNotification(notification)
    }
}