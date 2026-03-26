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

fun RadarrWebhookPayload.title(): String = movie?.title ?: DEFAULT_VALUE
fun RadarrWebhookPayload.year(): String = movie?.year?.toString() ?: DEFAULT_VALUE
fun RadarrWebhookPayload.imdbId(): String = movie?.imdbId ?: DEFAULT_VALUE
fun RadarrWebhookPayload.quality(): String = movieFile?.quality ?: DEFAULT_VALUE
fun RadarrWebhookPayload.requester(): String = movie?.tags?.requester() ?: DEFAULT_VALUE
