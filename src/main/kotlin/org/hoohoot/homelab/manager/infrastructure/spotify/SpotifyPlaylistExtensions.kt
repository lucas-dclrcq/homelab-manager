package org.hoohoot.homelab.manager.infrastructure.spotify

import org.hoohoot.homelab.manager.domain.AlbumInfo
import org.hoohoot.homelab.manager.domain.Artist
import org.hoohoot.homelab.manager.domain.Playlist
import org.hoohoot.homelab.manager.domain.Track


fun SpotifyTrack.toDomainModel(): Track = Track(
    name = this.name,
    album = this.album?.toDomainModel(),
    artists = this.artists?.map { it.toDomainModel() },
    durationMs = this.durationMs,
    trackNumber = this.trackNumber,
    id = this.id
)

fun SpotifyAlbum.toDomainModel(): AlbumInfo = AlbumInfo(
    totalTracks = this.totalTracks,
    id = this.id,
    name = this.name
)

fun SpotifyArtist.toDomainModel(): Artist = Artist(
    id = this.id,
    name = this.name
)

fun SpotifyPlaylist.toDomainModel(): Playlist = Playlist(
    name = this.name,
    tracks = this.tracks?.items?.map { it.track?.toDomainModel() }
)