package org.hoohoot.homelab.manager.notifications

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hoohoot.homelab.manager.notifications.arr.requester

@JsonIgnoreProperties(ignoreUnknown = true)
data class RadarrWebhookPayload(
    val eventType: String? = null,
    val movie: RadarrWebhookMovie? = null,
    val movieFile: RadarrWebhookMovieFile? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RadarrWebhookMovie(
    val id: Int? = null,
    val title: String? = null,
    val year: Int? = null,
    val imdbId: String? = null,
    val tags: List<String>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RadarrWebhookMovieFile(
    val quality: String? = null,
)

private const val DEFAULT_VALUE = "unknown"

data class Movie(
    val title: String,
    val year: String,
    val imdbId: String,
    val quality: String,
    val requester: String
) {
    companion object {
        fun from(payload: RadarrWebhookPayload): Movie = Movie(
            title = payload.movie?.title ?: DEFAULT_VALUE,
            year = payload.movie?.year?.toString() ?: DEFAULT_VALUE,
            imdbId = payload.movie?.imdbId ?: DEFAULT_VALUE,
            quality = payload.movieFile?.quality ?: DEFAULT_VALUE,
            requester = payload.movie?.tags?.requester() ?: DEFAULT_VALUE
        )
    }
}
