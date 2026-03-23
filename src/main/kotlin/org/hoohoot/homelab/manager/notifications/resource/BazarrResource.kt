package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.BazarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.NotificationBuilder
import org.hoohoot.homelab.manager.notifications.SubtitleDownload
import org.hoohoot.homelab.manager.notifications.arr.mediaKey
import org.hoohoot.homelab.manager.notifications.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@Path("/api/notifications/bazarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class BazarrResource(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleBazarrNotification(payload: BazarrWebhookPayload): Response {
        val subtitle = SubtitleDownload.from(payload)
        Log.info("Notifying subtitle downloaded for: ${subtitle.mediaTitle}")

        val episodeInfo = subtitle.episodeInfo?.let { "\n- 🎞️ Episode : $it" } ?: ""
        val notification = NotificationBuilder()
            .addTitle("💬 Subtitle Downloaded")
            .addInfoLine("${subtitle.mediaTitle} (${subtitle.year})$episodeInfo")
            .addInfoLine("🗣️ ${subtitle.language} subtitles ${subtitle.action} from ${subtitle.provider} (score: ${subtitle.score}%)")
            .buildNotification()

        val key = mediaKey(subtitle.mediaTitle, subtitle.year)
        val existingThread = notificationRepo.getThreadByMediaKey(key)
        matrixSender.sendMediaNotification(notification, existingThread)

        return Response.noContent().build()
    }
}
