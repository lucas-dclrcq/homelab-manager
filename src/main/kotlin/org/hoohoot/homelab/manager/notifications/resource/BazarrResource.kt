package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.BazarrWebhookPayload
import org.hoohoot.homelab.manager.notifications.SubtitleDownload
import org.hoohoot.homelab.manager.notifications.arr.mediaKey
import org.hoohoot.homelab.manager.notifications.matrix.MatrixConfiguration
import org.hoohoot.homelab.manager.notifications.matrix.sendNotification
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@Path("/api/notifications/bazarr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class BazarrResource(
    private val matrixClient: MatrixClientServerApiClient,
    private val matrixConfig: MatrixConfiguration,
    private val notificationRepo: NotificationSentRepository,
) {

    @POST
    suspend fun handleBazarrNotification(payload: BazarrWebhookPayload): Response {
        val subtitle = SubtitleDownload.from(payload)
        Log.info("Notifying subtitle downloaded for: ${subtitle.mediaTitle}")

        val episodeInfo = subtitle.episodeInfo?.let { "\n- 🎞️ Episode : $it" } ?: ""
        val episodeInfoHtml = subtitle.episodeInfo?.let { "<br>- 🎞️ Episode : $it" } ?: ""

        val content = RoomMessageEventContent.TextBased.Text(
            body = "💬 Subtitle Downloaded\n${subtitle.mediaTitle} (${subtitle.year})$episodeInfo\n🗣️ ${subtitle.language} subtitles ${subtitle.action} from ${subtitle.provider} (score: ${subtitle.score}%)",
            format = "org.matrix.custom.html",
            formattedBody = "<h1>💬 Subtitle Downloaded</h1><p>${subtitle.mediaTitle} (${subtitle.year})$episodeInfoHtml<br>🗣️ ${subtitle.language} subtitles ${subtitle.action} from ${subtitle.provider} (score: ${subtitle.score}%)</p>"
        )

        val key = mediaKey(subtitle.mediaTitle, subtitle.year)
        val existingThread = notificationRepo.getThreadByMediaKey(key)
        matrixClient.sendNotification(content, matrixConfig.room().media(), existingThread)

        return Response.noContent().build()
    }
}
