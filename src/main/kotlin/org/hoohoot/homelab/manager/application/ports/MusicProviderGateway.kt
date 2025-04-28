package org.hoohoot.homelab.manager.application.ports

import org.hoohoot.homelab.manager.domain.Playlist


interface MusicProviderGateway {
    suspend fun getCurrentUserPlaylistAndTitles(): List<Playlist>?
}