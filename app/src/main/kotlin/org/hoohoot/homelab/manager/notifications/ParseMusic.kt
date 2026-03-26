package org.hoohoot.homelab.manager.notifications

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@JsonIgnoreProperties(ignoreUnknown = true)
data class LidarrWebhookPayload(
    val eventType: String? = null,
    val artist: LidarrWebhookArtist? = null,
    val album: LidarrWebhookAlbum? = null,
    val downloadClient: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LidarrWebhookArtist(
    val name: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LidarrWebhookAlbum(
    val title: String? = null,
    val releaseDate: String? = null,
    val genres: List<String>? = null,
    val images: List<LidarrWebhookImage>? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LidarrWebhookImage(
    val coverType: String? = null,
    val remoteUrl: String? = null,
)

private const val DEFAULT_VALUE = "unknown"

fun LidarrWebhookPayload.artistName(): String = artist?.name ?: DEFAULT_VALUE
fun LidarrWebhookPayload.albumTitle(): String = album?.title ?: DEFAULT_VALUE
fun LidarrWebhookPayload.coverUrl(): String = album?.images
    ?.firstOrNull { it.coverType == "cover" }
    ?.remoteUrl
    ?: DEFAULT_VALUE
fun LidarrWebhookPayload.genres(): List<String> = album?.genres ?: emptyList()
fun LidarrWebhookPayload.year(): String = album?.releaseDate
    ?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }.getOrNull() }
    ?.let { DateTimeFormatter.ofPattern("yyyy").format(it) }
    ?: DEFAULT_VALUE
fun LidarrWebhookPayload.source(): String = downloadClient ?: DEFAULT_VALUE
