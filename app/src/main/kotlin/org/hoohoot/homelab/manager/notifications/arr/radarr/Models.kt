package org.hoohoot.homelab.manager.notifications.arr.radarr

import org.hoohoot.homelab.manager.notifications.arr.HistoryQuality

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
    val imdbId: String? = null
)
