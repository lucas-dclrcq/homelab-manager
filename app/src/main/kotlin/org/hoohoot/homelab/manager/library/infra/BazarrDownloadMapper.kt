package org.hoohoot.homelab.manager.library.infra

import io.quarkus.logging.Log
import org.hoohoot.homelab.manager.shared.arr.bazarr.BazarrActions
import org.hoohoot.homelab.manager.shared.arr.bazarr.BazarrHistoryItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Format Bazarr : "1x02"
private val BAZARR_EPISODE_NUMBER_REGEX = Regex("""(\d+)x(\d+)""")
private val BAZARR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

internal fun String.parseInstantOrNull(): LocalDateTime? =
    runCatching { LocalDateTime.ofInstant(Instant.parse(this), ZoneId.systemDefault()) }.getOrNull()

internal fun BazarrHistoryItem.toMediaDownload(isEpisode: Boolean): MediaDownloadEntity? {
    if (action !in BazarrActions.DOWNLOAD_ACTIONS) return null
    val itemTitle = if (isEpisode) seriesTitle else title
    val downloadedAt = parseBazarrTimestamp()
    // Sans id de record côté Bazarr, la clé de dédup est synthétique
    val mediaId = if (isEpisode) sonarrEpisodeId else radarrId
    if (itemTitle.isNullOrBlank() || downloadedAt == null || mediaId == null) {
        Log.warn("Skipping Bazarr history item '$itemTitle': missing title, media id or unparseable timestamp")
        return null
    }
    val item = this
    val prefix = if (isEpisode) "e" else "m"
    val parsedEpisodeNumber = episodeNumber?.let { BAZARR_EPISODE_NUMBER_REGEX.find(it) }
    return MediaDownloadEntity().apply {
        source = MediaDownloadEntity.SOURCE_BAZARR
        externalId = "$prefix:$mediaId:${item.language?.code2 ?: item.language?.name}:${item.rawTimestamp ?: item.timestamp}"
        mediaType = MediaDownloadEntity.MEDIA_TYPE_SUBTITLES
        title = itemTitle
        seasonNumber = parsedEpisodeNumber?.groupValues?.get(1)?.toIntOrNull()
        episodeNumber = parsedEpisodeNumber?.groupValues?.get(2)?.toIntOrNull()
        episodeTitle = if (isEpisode) item.episodeTitle else null
        language = item.language?.name ?: item.language?.code2
        provider = item.provider
        this.downloadedAt = downloadedAt
    }
}

private fun BazarrHistoryItem.parseBazarrTimestamp(): LocalDateTime? {
    val raw = rawTimestamp ?: timestamp ?: return null
    return raw.parseInstantOrNull()
        ?: runCatching { LocalDateTime.parse(raw, BAZARR_TIMESTAMP_FORMATTER) }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw) }.getOrNull()
}
