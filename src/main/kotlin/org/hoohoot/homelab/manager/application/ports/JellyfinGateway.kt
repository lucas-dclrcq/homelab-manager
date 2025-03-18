package org.hoohoot.homelab.manager.application.ports

interface JellyfinGateway {
    suspend fun searchMedia(searchTerm: String): List<MediaSearchResult>
}

data class MediaSearchResult(val itemId: String, val name: String, val type: String)