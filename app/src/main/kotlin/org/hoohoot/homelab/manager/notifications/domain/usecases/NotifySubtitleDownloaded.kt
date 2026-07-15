package org.hoohoot.homelab.manager.notifications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.NotificationRoom
import org.hoohoot.homelab.manager.notifications.domain.mediaKey
import org.hoohoot.homelab.manager.notifications.domain.notificationMessage
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationSender
import org.hoohoot.homelab.manager.notifications.domain.ports.NotificationThreads

data class SubtitleDownload(
    val mediaTitle: String,
    val year: String,
    val language: String,
    val action: String,
    val provider: String,
    val score: String,
    val episodeInfo: String?,
)

@ApplicationScoped
class NotifySubtitleDownloaded(
    private val sender: NotificationSender,
    private val threads: NotificationThreads,
) {
    suspend operator fun invoke(subtitle: SubtitleDownload) {
        val message = notificationMessage(
            "💬 Subtitle Downloaded",
            listOfNotNull(
                "${subtitle.mediaTitle} (${subtitle.year})",
                subtitle.episodeInfo?.let { "- 🎞️ Episode : $it" },
                "🗣️ ${subtitle.language} subtitles ${subtitle.action} from ${subtitle.provider} (score: ${subtitle.score}%)",
            ),
        )

        val existingThread = threads.getThreadByMediaKey(mediaKey(subtitle.mediaTitle, subtitle.year))
        sender.send(NotificationRoom.MEDIA, message, existingThread)
    }
}
