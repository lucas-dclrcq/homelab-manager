package org.hoohoot.homelab.manager.portal.downloads

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.shared.arr.bazarr.BazarrActions
import org.hoohoot.homelab.manager.shared.arr.bazarr.BazarrHistoryItem
import org.hoohoot.homelab.manager.shared.arr.bazarr.BazarrRestClient
import org.hoohoot.homelab.manager.shared.arr.lidarr.LidarrRestClient
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient
import org.hoohoot.homelab.manager.corrector.domain.usecases.CompleteAwaitingWorkflows
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadEntity
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@ApplicationScoped
class DownloadsSyncService(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
    @param:RestClient private val bazarrRestClient: BazarrRestClient,
    @param:RestClient private val lidarrRestClient: LidarrRestClient,
    private val mediaDownloadRepository: MediaDownloadRepository,
    private val completeAwaitingWorkflows: CompleteAwaitingWorkflows,
    @param:ConfigProperty(name = "downloads-sync.backfill-days") private val backfillDays: Long,
    @param:ConfigProperty(name = "downloads-sync.bazarr.page-length") private val bazarrPageLength: Int,
) {
    companion object {
        private const val DOWNLOAD_IMPORTED_EVENT_TYPE = "downloadFolderImported"

        // Lidarr : un event par téléchargement d'album complet (vs trackFileImported, un par piste)
        private const val LIDARR_DOWNLOAD_IMPORTED_EVENT_TYPE = "downloadImported"

        // Format Bazarr : "1x02"
        private val BAZARR_EPISODE_NUMBER_REGEX = Regex("""(\d+)x(\d+)""")
        private val BAZARR_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    suspend fun syncRadarr() {
        val since = sinceFor(MediaDownloadEntity.SOURCE_RADARR)
        val records = radarrRestClient.getHistorySince(since.toApiDate(), includeMovie = true).orEmpty()
        val candidates = records
            .filter { it.eventType == DOWNLOAD_IMPORTED_EVENT_TYPE }
            .mapNotNull { record ->
                val movieTitle = record.movie?.title
                val downloadedAt = record.date?.parseInstantOrNull()
                if (record.id == null || movieTitle.isNullOrBlank() || downloadedAt == null) {
                    Log.warn("Skipping Radarr history record ${record.id}: missing id, movie title or date")
                    return@mapNotNull null
                }
                MediaDownloadEntity().apply {
                    source = MediaDownloadEntity.SOURCE_RADARR
                    externalId = record.id.toString()
                    mediaType = MediaDownloadEntity.MEDIA_TYPE_MOVIE
                    title = record.movie.year?.let { "$movieTitle ($it)" } ?: movieTitle
                    quality = record.quality?.quality?.name
                    this.downloadedAt = downloadedAt
                }
            }
        val inserted = mediaDownloadRepository.saveNewDownloads(MediaDownloadEntity.SOURCE_RADARR, candidates)
        Log.info("Radarr downloads sync: $inserted new download(s) since $since")

        val importsByMovie = records
            .filter { it.eventType == DOWNLOAD_IMPORTED_EVENT_TYPE }
            .mapNotNull { record ->
                val movieId = record.movieId ?: return@mapNotNull null
                val importedAt = record.date?.parseInstantOrNull() ?: return@mapNotNull null
                movieId to importedAt
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, dates) -> dates.max() }
        completeAwaitingWorkflows(importsByMovie)
    }

    suspend fun syncSonarr() {
        val since = sinceFor(MediaDownloadEntity.SOURCE_SONARR)
        val records = sonarrRestClient
            .getHistorySince(since.toApiDate(), includeSeries = true, includeEpisode = true)
            .orEmpty()
        val candidates = records
            .filter { it.eventType == DOWNLOAD_IMPORTED_EVENT_TYPE }
            .mapNotNull { record ->
                val seriesTitle = record.series?.title
                val downloadedAt = record.date?.parseInstantOrNull()
                if (record.id == null || seriesTitle.isNullOrBlank() || downloadedAt == null) {
                    Log.warn("Skipping Sonarr history record ${record.id}: missing id, series title or date")
                    return@mapNotNull null
                }
                MediaDownloadEntity().apply {
                    source = MediaDownloadEntity.SOURCE_SONARR
                    externalId = record.id.toString()
                    mediaType = MediaDownloadEntity.MEDIA_TYPE_EPISODE
                    title = seriesTitle
                    seasonNumber = record.episode?.seasonNumber
                    episodeNumber = record.episode?.episodeNumber
                    episodeTitle = record.episode?.title
                    quality = record.quality?.quality?.name
                    this.downloadedAt = downloadedAt
                }
            }
        val inserted = mediaDownloadRepository.saveNewDownloads(MediaDownloadEntity.SOURCE_SONARR, candidates)
        Log.info("Sonarr downloads sync: $inserted new download(s) since $since")
    }

    suspend fun syncLidarr() {
        val since = sinceFor(MediaDownloadEntity.SOURCE_LIDARR)
        val records = lidarrRestClient
            .getHistorySince(since.toApiDate(), includeAlbum = true, includeArtist = true)
            .orEmpty()
        val candidates = records
            .filter { it.eventType == LIDARR_DOWNLOAD_IMPORTED_EVENT_TYPE }
            .mapNotNull { record ->
                val albumTitle = record.album?.title
                val downloadedAt = record.date?.parseInstantOrNull()
                if (record.id == null || albumTitle.isNullOrBlank() || downloadedAt == null) {
                    Log.warn("Skipping Lidarr history record ${record.id}: missing id, album title or date")
                    return@mapNotNull null
                }
                MediaDownloadEntity().apply {
                    source = MediaDownloadEntity.SOURCE_LIDARR
                    externalId = record.id.toString()
                    mediaType = MediaDownloadEntity.MEDIA_TYPE_ALBUM
                    title = albumTitle
                    artist = record.artist?.artistName ?: record.album.artist?.artistName
                    quality = record.quality?.quality?.name
                    this.downloadedAt = downloadedAt
                }
            }
        val inserted = mediaDownloadRepository.saveNewDownloads(MediaDownloadEntity.SOURCE_LIDARR, candidates)
        Log.info("Lidarr downloads sync: $inserted new download(s) since $since")
    }

    suspend fun syncBazarr() {
        val episodeItems = bazarrRestClient.getEpisodesHistory(0, bazarrPageLength)?.data.orEmpty()
        val movieItems = bazarrRestClient.getMoviesHistory(0, bazarrPageLength)?.data.orEmpty()
        val candidates =
            episodeItems.mapNotNull { it.toEntity(isEpisode = true) } +
                movieItems.mapNotNull { it.toEntity(isEpisode = false) }
        val inserted = mediaDownloadRepository.saveNewDownloads(MediaDownloadEntity.SOURCE_BAZARR, candidates)
        Log.info("Bazarr subtitles sync: $inserted new download(s)")
    }

    private fun BazarrHistoryItem.toEntity(isEpisode: Boolean): MediaDownloadEntity? {
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

    private suspend fun sinceFor(source: String): LocalDateTime =
        // Overlap d'1h sur le watermark : la dédup rend le refetch inoffensif
        mediaDownloadRepository.latestDownloadedAt(source)?.minusHours(1)
            ?: LocalDateTime.now().minusDays(backfillDays)

    private fun LocalDateTime.toApiDate(): String =
        atZone(ZoneId.systemDefault()).toInstant().atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    private fun String.parseInstantOrNull(): LocalDateTime? =
        runCatching { LocalDateTime.ofInstant(Instant.parse(this), ZoneId.systemDefault()) }.getOrNull()

    private fun BazarrHistoryItem.parseBazarrTimestamp(): LocalDateTime? {
        val raw = rawTimestamp ?: timestamp ?: return null
        return raw.parseInstantOrNull()
            ?: runCatching { LocalDateTime.parse(raw, BAZARR_TIMESTAMP_FORMATTER) }.getOrNull()
            ?: runCatching { LocalDateTime.parse(raw) }.getOrNull()
    }
}
