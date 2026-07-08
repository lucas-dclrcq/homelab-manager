package org.hoohoot.homelab.manager.notifications.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hoohoot.homelab.manager.notifications.domain.usecases.SubtitleDownload

@JsonIgnoreProperties(ignoreUnknown = true)
data class BazarrWebhookPayload(
    val title: String? = null,
    val message: String? = null,
    val type: String? = null
)

private val SERIES_REGEX =
    """^(.+?) \((\d{4})\) - (S\d{2}E\d{2} - .+?) : (.+?) subtitles (.+?) from (.+?) with a score of ([\d.]+)%\.$""".toRegex()
private val MOVIE_REGEX =
    """^(.+?) \((\d{4})\) : (.+?) subtitles (.+?) from (.+?) with a score of ([\d.]+)%\.$""".toRegex()

fun BazarrWebhookPayload.toSubtitleDownload(): SubtitleDownload {
    val body = message
        ?: throw IllegalArgumentException("Bazarr webhook message is missing")

    val seriesMatch = SERIES_REGEX.matchEntire(body)
    if (seriesMatch != null) {
        val (title, year, episodeInfo, language, action, provider, score) = seriesMatch.destructured
        return SubtitleDownload(title, year, language, action, provider, score, episodeInfo)
    }

    val movieMatch = MOVIE_REGEX.matchEntire(body)
    if (movieMatch != null) {
        val (title, year, language, action, provider, score) = movieMatch.destructured
        return SubtitleDownload(title, year, language, action, provider, score, null)
    }

    throw IllegalArgumentException("Unable to parse Bazarr webhook body: $body")
}
