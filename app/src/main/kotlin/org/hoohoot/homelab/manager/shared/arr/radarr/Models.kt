package org.hoohoot.homelab.manager.shared.arr.radarr

import com.fasterxml.jackson.databind.JsonNode
import org.hoohoot.homelab.manager.shared.arr.HistoryQuality

data class RadarrHistoryRecord(
    val id: Long? = null,
    val movieId: Int? = null,
    val sourceTitle: String? = null,
    val date: String? = null,
    val eventType: String? = null,
    val quality: HistoryQuality? = null,
    val movie: RadarrMovie? = null
)

data class RadarrMovie(
    val title: String? = null,
    val year: Int? = null,
    val digitalRelease: String? = null,
    val physicalRelease: String? = null,
    val inCinemas: String? = null,
    val id: Int? = null,
    val imdbId: String? = null,
    val tmdbId: Int? = null,
    val overview: String? = null,
    val hasFile: Boolean? = null,
    val qualityProfileId: Int? = null,
    val sizeOnDisk: Long? = null,
    val added: String? = null,
    val tags: List<Int> = emptyList(),
    val images: List<RadarrImage> = emptyList(),
    val movieFile: RadarrMovieFile? = null
)

data class RadarrQualityProfile(
    val id: Int? = null,
    val name: String? = null,
    val cutoff: Int? = null,
    val items: List<RadarrQualityProfileItem> = emptyList()
)

data class RadarrQualityProfileItem(
    val id: Int? = null,
    val name: String? = null,
    val allowed: Boolean? = null,
    val quality: RadarrQualityDefinition? = null,
    val items: List<RadarrQualityProfileItem> = emptyList()
)

data class RadarrQualityDefinition(
    val id: Int? = null,
    val name: String? = null,
    val resolution: Int? = null
)

data class RadarrImage(
    val coverType: String? = null,
    val remoteUrl: String? = null
)

data class RadarrMovieFile(
    val quality: HistoryQuality? = null,
    val languages: List<RadarrLanguage> = emptyList(),
    val size: Long? = null,
    val dateAdded: String? = null
)

data class RadarrLanguage(
    val id: Int? = null,
    val name: String? = null
)

data class RadarrRelease(
    val guid: String? = null,
    val indexerId: Int? = null,
    val indexer: String? = null,
    val title: String? = null,
    val quality: HistoryQuality? = null,
    val languages: List<RadarrLanguage> = emptyList(),
    val size: Long? = null,
    val age: Int? = null,
    val seeders: Int? = null,
    val leechers: Int? = null,
    val protocol: String? = null,
    val rejected: Boolean? = null,
    val rejections: List<String> = emptyList()
)

data class RadarrGrabRequest(
    val guid: String,
    val indexerId: Int
)

data class RadarrQueuePage(
    val totalRecords: Int? = null,
    val records: List<RadarrQueueRecord> = emptyList()
)

data class RadarrQueueRecord(
    val id: Long? = null,
    val movieId: Int? = null,
    val downloadId: String? = null,
    val title: String? = null,
    val trackedDownloadState: String? = null,
    val statusMessages: List<RadarrQueueStatusMessage> = emptyList()
)

data class RadarrQueueStatusMessage(
    val title: String? = null,
    val messages: List<String> = emptyList()
)

// quality/languages sont relayés tels quels dans la commande ManualImport : Radarr attend le
// QualityModel complet, que les DTOs history (lossy) ne portent pas → passthrough JsonNode
data class RadarrManualImportItem(
    val path: String? = null,
    val downloadId: String? = null,
    val movie: RadarrMovie? = null,
    val quality: JsonNode? = null,
    val languages: JsonNode? = null,
    val rejections: List<RadarrImportRejection> = emptyList()
)

data class RadarrImportRejection(
    val reason: String? = null,
    val type: String? = null
)

data class RadarrManualImportCommand(
    val files: List<RadarrManualImportFile>,
    val importMode: String = "auto",
    val name: String = "ManualImport"
)

data class RadarrManualImportFile(
    val path: String,
    val movieId: Int,
    val quality: JsonNode?,
    val languages: JsonNode?,
    val downloadId: String
)

data class RadarrCommandResource(
    val id: Long? = null,
    val name: String? = null,
    val status: String? = null
)
