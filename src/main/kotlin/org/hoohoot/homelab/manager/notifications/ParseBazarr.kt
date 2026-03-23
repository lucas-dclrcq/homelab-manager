package org.hoohoot.homelab.manager.notifications

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BazarrWebhookPayload(
    val title: String? = null,
    val message: String? = null,
    val type: String? = null
)

data class SubtitleDownload(
    val mediaTitle: String,
    val year: String,
    val language: String,
    val action: String,
    val provider: String,
    val score: String,
    val episodeInfo: String?
) {
    companion object {
        private val SERIES_REGEX =
            """^(.+?) \((\d{4})\) - (S\d{2}E\d{2} - .+?) : (.+?) subtitles (.+?) from (.+?) with a score of ([\d.]+)%\.$""".toRegex()
        private val MOVIE_REGEX =
            """^(.+?) \((\d{4})\) : (.+?) subtitles (.+?) from (.+?) with a score of ([\d.]+)%\.$""".toRegex()

        fun from(payload: BazarrWebhookPayload): SubtitleDownload {
            val body = payload.message
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
    }
}
