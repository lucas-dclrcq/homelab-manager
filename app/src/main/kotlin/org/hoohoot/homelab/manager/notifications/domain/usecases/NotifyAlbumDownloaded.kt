package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender

data class AlbumDownload(
    val artistName: String,
    val albumTitle: String,
    val year: String,
    val coverUrl: String,
    val genres: List<String>,
    val source: String,
)

@ApplicationScoped
class NotifyAlbumDownloaded(private val sender: NotificationSender) {
    suspend operator fun invoke(album: AlbumDownload) {
        val message = notificationMessage(
            "🎵 Album downloaded",
            listOf(
                "${album.artistName} - ${album.albumTitle} (${album.year})",
                "🖼️ Cover: ${album.coverUrl}",
                "🎸 Genres : ${album.genres.joinToString(", ")}",
                "📥 Source : ${album.source}",
            ),
        )
        sender.send(NotificationRoom.MUSIC, message)
    }
}
