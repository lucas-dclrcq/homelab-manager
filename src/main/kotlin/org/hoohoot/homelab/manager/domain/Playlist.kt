package org.hoohoot.homelab.manager.domain

data class Playlist(val name: String?,  val tracks: List<Track?>?)

data class Track(
    val album: AlbumInfo?,
    val artists: List<Artist>?,
    val durationMs: Int?,
    val id: String?,
    val name: String?,
    val trackNumber: Int?,
)
data class AlbumInfo(
    val totalTracks: Int?,
    val id: String?,
    val name: String?,
)

data class Artist(
    val id: String?,
    val name: String?,
)

