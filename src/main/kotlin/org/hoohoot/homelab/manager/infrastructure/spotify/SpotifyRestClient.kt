package org.hoohoot.homelab.manager.infrastructure.spotify

import com.fasterxml.jackson.annotation.JsonProperty
import io.quarkus.oidc.token.propagation.common.AccessToken
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "spotify-api")
@AccessToken
@Produces(MediaType.APPLICATION_JSON)
interface SpotifyRestClient {

    @GET
    @Path("/me/playlists")
    suspend fun getCurrentUserPlaylists(): SpotifyUserPlaylistsResponse

    @GET
    @Path("/playlists/{playlistId}")
    suspend fun getPlaylistById(playlistId: String): SpotifyPlaylist
}

data class SpotifyUserPlaylistsResponse(
    val href: String? = null,
    val limit: Int? = null,
    val next: String? = null,
    val offset: Int? = null,
    val previous: String? = null,
    val total: Int? = null,
    val items: List<SpotifyPlaylistItem>? = null
)

data class SpotifyPlaylistItem(
    val collaborative: Boolean? = null,
    val description: String? = null,
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val images: List<SpotifyImage>? = null,
    val name: String? = null,
    val owner: SpotifyOwner? = null,
    @JsonProperty("primary_color") val primaryColor: String? = null,
    val public: Boolean? = null,
    @JsonProperty("snapshot_id") val snapshotId: String? = null,
    val tracks: SpotifyTracks? = null,
    val type: String? = null,
    val uri: String? = null
)

data class SpotifyImage(
    val height: Int? = null,
    val url: String? = null,
    val width: Int? = null
)

data class SpotifyOwner(
    @JsonProperty("display_name") val displayName: String? = null,
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val type: String? = null,
    val uri: String? = null
)

data class SpotifyPlaylist(
    val collaborative: Boolean? = null,
    val description: String? = null,
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val images: List<SpotifyImage>? = null,
    val name: String? = null,
    val owner: SpotifyOwner? = null,
    val public: Boolean? = null,
    @JsonProperty("snapshot_id") val snapshotId: String? = null,
    val tracks: SpotifyTracks? = null,
    val type: String? = null,
    val uri: String? = null
)

data class SpotifyExternalUrl(
    val spotify: String? = null
)

data class SpotifyTracks(
    val href: String? = null,
    val limit: Int? = null,
    val next: String? = null,
    val offset: Int? = null,
    val previous: String? = null,
    val total: Int? = null,
    val items: List<SpotifyTrackItem>? = null
)

data class SpotifyTrackItem(
    @JsonProperty("added_at") val addedAt: String? = null,
    @JsonProperty("added_by") val addedBy: SpotifyAddedBy? = null,
    @JsonProperty("is_local") val isLocal: Boolean? = null,
    val track: SpotifyTrack? = null
)

data class SpotifyAddedBy(
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val type: String? = null,
    val uri: String? = null
)

data class SpotifyTrack(
    val album: SpotifyAlbum? = null,
    val artists: List<SpotifyArtist>? = null,
    @JsonProperty("available_markets") val availableMarkets: List<String>? = null,
    @JsonProperty("disc_number") val discNumber: Int? = null,
    @JsonProperty("duration_ms") val durationMs: Int? = null,
    val explicit: Boolean? = null,
    @JsonProperty("external_ids") val externalIds: SpotifyExternalIds? = null,
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    @JsonProperty("is_playable") val isPlayable: Boolean? = null,
    @JsonProperty("linked_from") val linkedFrom: Any? = null, // This could be better typed if structure is known
    val restrictions: SpotifyRestrictions? = null,
    val name: String? = null,
    val popularity: Int? = null,
    @JsonProperty("preview_url") val previewUrl: String? = null,
    @JsonProperty("track_number") val trackNumber: Int? = null,
    val type: String? = null,
    val uri: String? = null,
    @JsonProperty("is_local") val isLocal: Boolean? = null
)

data class SpotifyAlbum(
    @JsonProperty("album_type") val albumType: String? = null,
    @JsonProperty("total_tracks") val totalTracks: Int? = null,
    @JsonProperty("available_markets") val availableMarkets: List<String>? = null,
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val images: List<SpotifyImage>? = null,
    val name: String? = null,
    @JsonProperty("release_date") val releaseDate: String? = null,
    @JsonProperty("release_date_precision") val releaseDatePrecision: String? = null,
    val restrictions: SpotifyRestrictions? = null,
    val type: String? = null,
    val uri: String? = null,
    val artists: List<SpotifyArtist>? = null
)

data class SpotifyArtist(
    @JsonProperty("external_urls") val externalUrls: SpotifyExternalUrl? = null,
    val href: String? = null,
    val id: String,
    val name: String? = null,
    val type: String? = null,
    val uri: String? = null
)

data class SpotifyExternalIds(
    val isrc: String? = null,
    val ean: String? = null,
    val upc: String? = null
)

data class SpotifyRestrictions(
    val reason: String? = null
)
