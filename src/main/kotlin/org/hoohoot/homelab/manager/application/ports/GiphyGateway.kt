package org.hoohoot.homelab.manager.application.ports

import org.hoohoot.homelab.manager.application.queries.Gif

data class NoGifFoundException(val query: String) : Exception("No GIF found for query: $query")

interface GifGateway {
    suspend fun searchGif(query: String): Gif
}