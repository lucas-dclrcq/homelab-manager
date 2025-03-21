package org.hoohoot.homelab.manager.infrastructure.giphy

import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.ext.web.client.WebClient
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.GifGateway
import org.hoohoot.homelab.manager.application.ports.NoGifFoundException
import org.hoohoot.homelab.manager.application.queries.Gif

@ApplicationScoped
class GiphyGifGateway(@RestClient private val restClient: GiphyRestClient, vertx: Vertx): GifGateway {
    private val webClient = WebClient.create(vertx)

    override suspend fun searchGif(query: String): Gif {
        val gifResult = this.restClient.search(query)

        val originalGif = gifResult.data?.first()?.images?.original ?: throw NoGifFoundException(query)

        val gifUrl = originalGif.url
        val height = originalGif.height?.toInt() ?: 0
        val width = originalGif.width?.toInt() ?: 0

        return webClient
            .getAbs(gifUrl)
            .send()
            .awaitSuspending()
            .bodyAsBuffer()
            .bytes
            .let { Gif(it, height, width) }
    }
}