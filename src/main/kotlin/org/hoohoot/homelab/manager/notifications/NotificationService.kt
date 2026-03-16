package org.hoohoot.homelab.manager.notifications

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.matrix.MatrixNotificationSender
import org.hoohoot.homelab.manager.persistence.NotificationSentRepository
import org.hoohoot.homelab.manager.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.sonarr.getSeriesCalendar
import org.hoohoot.homelab.manager.time.TimeService

private const val DEFAULT_VALUE = "unknown"

@ApplicationScoped
class NotificationService(
    private val matrixSender: MatrixNotificationSender,
    private val notificationRepo: NotificationSentRepository,
    @RestClient private val sonarrRestClient: SonarrRestClient,
    private val timeService: TimeService
) {

    suspend fun notifyMovieDownloaded(payload: RadarrWebhookPayload) {
        val movie = Movie.from(payload)
        Log.info("Notifying movie downloaded : ${movie.title}")

        val notification = NotificationBuilder()
            .addTitle("Movie Downloaded")
            .addInfoLine("${movie.title} (${movie.year}) [${movie.quality}] ${movie.imdbId.toImdbLink()}")
            .addInfoLine("Requested by : ${movie.requester}")
            .buildNotification()

        val sentId = matrixSender.sendMediaNotification(notification)

        val movieId = payload.movie?.id?.toString()
        val title = payload.movie?.title
        val year = payload.movie?.year
        if (movieId != null) {
            val key = if (title != null && year != null) mediaKey(title, year.toString()) else null
            notificationRepo.saveOrUpdateThread(movieId, "movie", key, sentId)
        }
    }

    suspend fun notifyEpisodeDownloaded(payload: SonarrWebhookPayload) {
        Log.info("Notifying series downloaded : ${payload.seriesName()}")

        val notification = NotificationBuilder()
            .addTitle("Episode Downloaded")
            .addInfoLine("Series : ${payload.series?.title ?: "Unknown"} [${payload.imdbId().toImdbLink()}]")
            .addInfoLine("Episode : ${payload.seasonAndEpisodeNumber()} - ${payload.episodeName()} [${payload.quality()}]")
            .addInfoLine("Series requested by : ${payload.requester()}")
            .addInfoLine("Source : ${payload.downloadClient} (${payload.indexer()})")
            .buildNotification()

        val seriesId = payload.series?.id?.toString()

        if (seriesId != null) {
            val activeThread = notificationRepo.getThreadByMediaId(seriesId, "series")
            val sentNotificationId = matrixSender.sendMediaNotification(notification, activeThread)
            val threadEventId = activeThread ?: sentNotificationId

            val title = payload.series.title
            val year = payload.series.year
            val key = if (title != null && year != null) mediaKey(title, year.toString()) else null
            notificationRepo.saveOrUpdateThread(seriesId, "series", key, threadEventId)
        } else {
            matrixSender.sendMediaNotification(notification)
        }
    }

    suspend fun notifySubtitleDownloaded(payload: BazarrWebhookPayload) {
        val subtitle = SubtitleDownload.from(payload)
        Log.info("Notifying subtitle downloaded for: ${subtitle.mediaTitle}")

        val episodeInfo = subtitle.episodeInfo?.let { "\n- Episode : $it" } ?: ""
        val notification = NotificationBuilder()
            .addTitle("Subtitle Downloaded")
            .addInfoLine("${subtitle.mediaTitle} (${subtitle.year})$episodeInfo")
            .addInfoLine("${subtitle.language} subtitles ${subtitle.action} from ${subtitle.provider} (score: ${subtitle.score}%)")
            .buildNotification()

        val key = mediaKey(subtitle.mediaTitle, subtitle.year)
        val existingThread = notificationRepo.getThreadByMediaKey(key)
        matrixSender.sendMediaNotification(notification, existingThread)
    }

    suspend fun notifyAlbumDownloaded(payload: LidarrWebhookPayload) {
        val album = Album.from(payload)
        Log.info("Notifying album downloaded : ${album.albumTitle}")

        val notification = NotificationBuilder()
            .addTitle("Album downloaded")
            .addInfoLine("${album.artistName} - ${album.albumTitle} (${album.year})")
            .addInfoLine("Cover: ${album.coverUrl}")
            .addInfoLine("Genres : ${album.genres.joinToString(", ")}")
            .addInfoLine("Source : ${album.downloadClient}")
            .buildNotification()

        matrixSender.sendMusicNotification(notification)
    }

    suspend fun handleJellyseerrEvent(issue: Issue) {
        when (issue.notificationType) {
            "ISSUE_CREATED" -> handleIssueCreated(issue)
            "ISSUE_RESOLVED" -> handleIssueReply(issue)
            "ISSUE_COMMENT" -> handleIssueComment(issue)
            else -> Log.warn("Unhandled jellyseerr type: ${issue.notificationType}")
        }
    }

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

    private suspend fun handleIssueReply(issue: Issue) {
        Log.info("Notifying issue resolved : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Message : ${issue.message}")
            .addInfoLine("Reporter : ${issue.reportedByUserName}")
            .buildNotification()

        sendSupportNotificationInThread(notification, issue.id)
    }

    private suspend fun handleIssueComment(issue: Issue) {
        Log.info("Notifying issue commented : ${issue.title}")

        val notification = NotificationBuilder()
            .addTitle(issue.title)
            .addInfoLine("Subject : ${issue.subject}")
            .addInfoLine("Comment : ${issue.comment ?: "No comment"}")
            .addInfoLine("Comment by : ${issue.commentedBy ?: "No reporter"}")
            .buildNotification()

        sendSupportNotificationInThread(notification, issue.id)
    }

    private suspend fun sendSupportNotificationInThread(notification: Notification, issueId: String) {
        val issueCreatedNotificationId = notificationRepo.getNotificationIdForIssue(issueId)

        if (issueCreatedNotificationId != null) {
            matrixSender.sendSupportNotification(notification, issueCreatedNotificationId)
        } else {
            matrixSender.sendSupportNotification(notification)
        }
    }
}

// Sonarr payload extension functions

private fun SonarrWebhookPayload.quality(): String = this.episodeFile
    ?.quality
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.seriesName(): String = this.series
    ?.title
    ?: DEFAULT_VALUE

private fun SonarrWebhookPayload.seasonAndEpisodeNumber(): String = this.episodes
    ?.firstOrNull()
    ?.let { "S%02dE%02d".format(it.seasonNumber, it.episodeNumber) }
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
