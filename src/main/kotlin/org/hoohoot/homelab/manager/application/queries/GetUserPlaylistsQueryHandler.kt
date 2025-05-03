package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.MusicProviderGateway
import org.hoohoot.homelab.manager.domain.AlbumInfo
import org.hoohoot.homelab.manager.domain.Artist
import org.hoohoot.homelab.manager.domain.Playlist
import org.hoohoot.homelab.manager.domain.Track

class GetUserPlaylists : Query<List<PlaylistDto>?>

@Startup
@ApplicationScoped
class GetUserPlaylistsQueryHandler(private val playlistGateway: MusicProviderGateway) :
    QueryHandler<GetUserPlaylists, List<PlaylistDto>?> {
    override suspend fun handle(query: GetUserPlaylists): List<PlaylistDto>? =
        playlistGateway.getCurrentUserPlaylistAndTitles()?.map { playlist -> playlist.toDto() }
}

data class PlaylistDto(val name: String?,  val tracks: List<TrackDto?>?)

data class TrackDto(
    val album: AlbumInfoDto?,
    val artists: List<ArtistDto>?,
    val durationMs: Int?,
    val id: String?,
    val name: String?,
    val trackNumber: Int?,
)
data class AlbumInfoDto(
    val totalTracks: Int?,
    val id: String?,
    val name: String?,
)

data class ArtistDto(
    val id: String?,
    val name: String?,
)

fun Playlist.toDto(): PlaylistDto = PlaylistDto(
    name = this.name,
    tracks = this.tracks?.map { track -> track?.toDto() }
)

private fun Track.toDto(): TrackDto = TrackDto(
    name = this.name,
    album = this.album?.toDto(),
    artists = this.artists?.map { artist -> artist.toDto() },
    durationMs = this.durationMs,
    id = this.id,
    trackNumber = this.trackNumber
)

private fun AlbumInfo.toDto(): AlbumInfoDto = AlbumInfoDto(
    id = this.id,
    name = this.name,
    totalTracks = this.totalTracks
)
private fun Artist.toDto(): ArtistDto = ArtistDto(
    id = this.id,
    name = this.name
)

