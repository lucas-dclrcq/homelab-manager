package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationThreads

data class EpisodeDownload(
    val seriesTitle: String,
    val imdbLink: String,
    val seasonAndEpisode: String,
    val episodeTitle: String,
    val quality: String,
    val requester: String,
    val downloadClient: String?,
    val indexer: String,
    val seriesId: String?,
    val mediaKey: String?,
)

@ApplicationScoped
class NotifyEpisodeDownloaded(
    private val sender: NotificationSender,
    private val threads: NotificationThreads,
) {
    suspend operator fun invoke(episode: EpisodeDownload) {
        val message = notificationMessage(
            "📺 Episode Downloaded",
            listOf(
                "📡 Series : ${episode.seriesTitle} [${episode.imdbLink}]",
                "🎞️ Episode : ${episode.seasonAndEpisode} - ${episode.episodeTitle} [${episode.quality}]",
                "👤 Series requested by : ${episode.requester}",
                "📥 Source : ${episode.downloadClient} (${episode.indexer})",
            ),
        )

        // Les épisodes d'une même série sont regroupés dans un thread Matrix
        if (episode.seriesId != null) {
            val activeThread = threads.getThreadByMediaId(episode.seriesId, "series")
            val sentNotificationId = sender.send(NotificationRoom.MEDIA, message, activeThread)
            val threadEventId = activeThread ?: sentNotificationId
            threads.saveOrUpdateThread(episode.seriesId, "series", episode.mediaKey, threadEventId)
        } else {
            sender.send(NotificationRoom.MEDIA, message)
        }
    }
}
