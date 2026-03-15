package org.hoohoot.homelab.manager.notifications

import io.quarkus.logging.Log
import io.vertx.core.json.JsonObject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.persistence.NotificationSentRepository
import org.hoohoot.homelab.manager.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.sonarr.getSeriesCalendar
import org.hoohoot.homelab.manager.time.TimeService

private const val DEFAULT_VALUE = "unknown"

@Path("/api/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class NotificationsResource(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
    @RestClient private val sonarrRestClient: SonarrRestClient,
    private val timeService: TimeService
) {

    @POST
    @Path("/radarr")
    suspend fun handleRadarrNotification(payload: JsonObject): Response {
        if (payload.getString("eventType") != "Download") {
            Log.debug("Ignoring radarr event: ${payload.getString("eventType")}")
            return Response.noContent().build()
        }

        val movie = ParseMovie.from(payload)
        Log.info("Notifying movie downloaded : ${movie.title}")

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("Requested by : ${movie.requester}")
            .buildNotification()

        matrixSender.sendMediaNotification(notification)
        return Response.noContent().build()
    }

    @POST
    @Path("/sonarr")
    suspend fun handleSonarrNotification(payload: SonarrWebhookPayload): Response {
        if (payload.eventType != "Download") {
            Log.info("Ignoring sonarr event: ${payload.eventType}")
            return Response.noContent().build()
        }

        Log.info("Notifying series downloaded : ${payload.seriesName()}")

        val seriesId = payload.imdbId()

        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${payload.series?.title ?: "Unknown"} [${seriesId.toImdbLink()}]")
            .addInfoLine("Episode : ${payload.seasonAndEpisodeNumber()} - ${payload.episodeName()} [${payload.quality()}]")
            .addInfoLine("Series requested by : ${payload.requester()}")
            .addInfoLine("Source : ${payload.downloadClient} (${payload.indexer()})")
            .buildNotification()

        matrixSender.sendMediaNotification(notification)
        return Response.noContent().build()
    }

    @POST
    @Path("/lidarr")
    suspend fun handleLidarrNotification(payload: JsonObject): Response {
        if (payload.getString("eventType") != "Download") {
            Log.debug("Ignoring lidarr event: ${payload.getString("eventType")}")
            return Response.noContent().build()
        }

        val album = ParseMusic.from(payload)
        Log.info("Notifying album downloaded : ${album.albumTitle}")

        val notification = NotificationBuilder()
            .addTitle("Album downloaded")
            .addInfoLine("${album.artistName} - ${album.albumTitle} (${album.year})")
            .addInfoLine("Cover: ${album.coverUrl}")
            .addInfoLine("Genres : ${album.genres.joinToString(", ")}")
            .addInfoLine("Source : ${album.downloadClient}")
            .buildNotification()

        matrixSender.sendMusicNotification(notification)
        return Response.noContent().build()
    }

    @POST
    @Path("/jellyseerr")
    suspend fun handleJellyseerrNotification(payload: JsonObject): Response {
        val issue = ParseIssue.from(payload)

        when (issue.notificationType) {
            "ISSUE_CREATED" -> handleIssueCreated(issue)
            "ISSUE_RESOLVED" -> handleIssueReplyNotification(issue)
            "ISSUE_COMMENT" -> handleIssueCommentNotification(issue)
            else -> Log.warn("Unhandled jellyseerr type: ${issue.notificationType}")
        }

        return Response.noContent().build()
    }

    @POST
    @Path("/send-whats-next-report")
    suspend fun sendWhatsNextReport() {
        Log.info("Sending whats next report")

        val currentWeek = timeService.getCurrentWeek()
        val seriesCalendar = sonarrRestClient.getSeriesCalendar(currentWeek.start, currentWeek.end)

        val scheduledSeries = seriesCalendar
            .map { "${it.airDate} : ${it.series?.title} - ${"S%02dE%02d".format(it.seasonNumber, it.episodeNumber)} - ${it.title}" }

        val notification = NotificationBuilder()
            .addTitle("What's next report")
            .addEmptyLine()
            .addInfoLine("Series :")
            .addInfoLines(scheduledSeries)
            .buildNotification()

        matrixSender.sendMediaNotification(notification)
    }

    private suspend fun handleIssueCreated(issue: Issue) {
        Log.info("Notifying issue created : ${issue.title}")

        var notificationBuilder = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")

        if (issue.additionalInfo.isNotEmpty()) {
            notificationBuilder = notificationBuilder
                .addInfoLine("Additional infos :")
                .addInfoLines(issue.additionalInfo.map { "- ${it.key} : ${it.value}" })
        }

        val notification = notificationBuilder.buildNotification()
        val sentNotificationId = matrixSender.sendSupportNotification(notification)
        notificationRepo.saveNotificationIdForIssue(issue.id, sentNotificationId)
    }

    private suspend fun handleIssueReplyNotification(issue: Issue) {
        Log.info("Notifying issue resolved : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issue.id)

        if (issueCreatedNotificationId != null) {
            matrixSender.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            matrixSender.sendSupportNotification(notification)
        }
    }

    private suspend fun handleIssueCommentNotification(issue: Issue) {
        Log.info("Notifying issue commented : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Comment : ${issue.comment ?: "No comment"}")
            .addInfoLine("Comment by : ${issue.commentedBy ?: "No reporter"}")
            .buildNotification()

        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issue.id)

        if (issueCreatedNotificationId != null) {
            matrixSender.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            matrixSender.sendSupportNotification(notification)
        }
    }

    private fun SonarrWebhookPayload.quality(): String = this.episodeFile
        ?.quality
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.seriesName(): String = this.series
        ?.title
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.seasonAndEpisodeNumber(): String = this.episodes
        ?.firstOrNull()
        ?.let {
            val episodeNumber = it.episodeNumber
            val seasonNumber = it.seasonNumber
            "S%02dE%02d".format(seasonNumber, episodeNumber)
        }
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.episodeName(): String = this.episodes
        ?.firstOrNull()
        ?.title
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.indexer(): String = this.release
        ?.indexer
        ?.replace(" (Prowlarr)", "")
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.imdbId(): String = this.series
        ?.imdbId
        ?: DEFAULT_VALUE

    private fun SonarrWebhookPayload.requester(): String = this.series?.tags
        ?.requester()
        ?: DEFAULT_VALUE
}
