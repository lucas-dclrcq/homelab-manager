package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationThreads

data class MovieDownload(
    val title: String,
    val year: String,
    val quality: String,
    val imdbLink: String,
    val requester: String,
    val movieId: String?,
    val mediaKey: String?,
)

@ApplicationScoped
class NotifyMovieDownloaded(
    private val sender: NotificationSender,
    private val threads: NotificationThreads,
) {
    suspend operator fun invoke(movie: MovieDownload) {
        val message = notificationMessage(
            "🎬 Movie Downloaded",
            listOf(
                "${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbLink}",
                "👤 Requested by : ${movie.requester}",
            ),
        )
        val sentId = sender.send(NotificationRoom.MEDIA, message)

        if (movie.movieId != null) {
            threads.saveOrUpdateThread(movie.movieId, "movie", movie.mediaKey, sentId)
        }
    }
}
