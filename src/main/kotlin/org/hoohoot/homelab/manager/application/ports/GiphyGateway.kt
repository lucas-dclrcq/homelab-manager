package org.hoohoot.homelab.manager.application.ports

import org.hoohoot.homelab.manager.application.queries.Gif


interface GifGateway {
    suspend fun searchGif(query: String): Gif
}