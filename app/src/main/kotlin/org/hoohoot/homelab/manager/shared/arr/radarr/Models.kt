package org.hoohoot.homelab.manager.shared.arr.radarr

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
    val overview: String? = null,
    val hasFile: Boolean? = null,
    val images: List<RadarrImage> = emptyList(),
    val movieFile: RadarrMovieFile? = null
)

data class RadarrImage(
    val coverType: String? = null,
    val remoteUrl: String? = null
)

data class RadarrMovieFile(
    val quality: HistoryQuality? = null,
    val languages: List<RadarrLanguage> = emptyList()
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
