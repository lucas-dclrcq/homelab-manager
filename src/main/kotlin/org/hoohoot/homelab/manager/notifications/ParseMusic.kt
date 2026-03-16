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

data class Album(
    val downloadClient: String,
    val artistName: String,
    val albumTitle: String,
    val coverUrl: String,
    val genres: List<String>,
    val year: String
) {
    companion object {
        fun from(payload: LidarrWebhookPayload): Album = Album(
            downloadClient = payload.downloadClient ?: DEFAULT_VALUE,
            artistName = payload.artist?.name ?: DEFAULT_VALUE,
            albumTitle = payload.album?.title ?: DEFAULT_VALUE,
            coverUrl = payload.album?.images
                ?.firstOrNull { it.coverType == "cover" }
                ?.remoteUrl
                ?: DEFAULT_VALUE,
            genres = payload.album?.genres ?: emptyList(),
            year = payload.album?.releaseDate
                ?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_ZONED_DATE_TIME) }.getOrNull() }
                ?.let { DateTimeFormatter.ofPattern("yyyy").format(it) }
                ?: DEFAULT_VALUE
        )
    }
}
