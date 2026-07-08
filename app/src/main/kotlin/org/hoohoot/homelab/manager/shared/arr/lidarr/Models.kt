package org.hoohoot.homelab.manager.shared.arr.lidarr

import org.hoohoot.homelab.manager.shared.arr.HistoryQuality

data class LidarrHistoryRecord(
    val id: Long? = null,
    val albumId: Int? = null,
    val artistId: Int? = null,
    val sourceTitle: String? = null,
    val date: String? = null,
    val eventType: String? = null,
    val quality: HistoryQuality? = null,
    val album: LidarrAlbum? = null,
    val artist: LidarrArtist? = null
)

data class LidarrAlbum(
    val title: String? = null,
    val releaseDate: String? = null,
    val artist: LidarrArtist? = null,
    val id: Int? = null
)

data class LidarrArtist(
    val artistName: String? = null,
    val id: Int? = null
)
