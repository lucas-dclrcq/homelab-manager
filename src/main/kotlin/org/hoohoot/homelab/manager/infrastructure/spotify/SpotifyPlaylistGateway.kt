package org.hoohoot.homelab.manager.infrastructure.spotify

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.MusicProviderGateway
import org.hoohoot.homelab.manager.domain.Playlist

@ApplicationScoped
class SpotifyPlaylistGateway(
    @RestClient private val spotifyRestClient: SpotifyRestClient,
) : MusicProviderGateway {

    override suspend fun getCurrentUserPlaylistAndTitles(): List<Playlist>? =
        spotifyRestClient.getCurrentUserPlaylists().items?.map { playlistItem ->
            val detailedPlaylist = spotifyRestClient.getPlaylistById(playlistItem.id)
            detailedPlaylist.toDomainModel()
        }
}