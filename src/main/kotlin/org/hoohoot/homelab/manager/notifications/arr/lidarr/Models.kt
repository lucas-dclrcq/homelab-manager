package org.hoohoot.homelab.manager.notifications.arr.lidarr

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
