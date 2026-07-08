package org.hoohoot.homelab.manager.notifications.infra.giphy

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class GiphyService(
    @param:RestClient private val restClient: GiphyRestClient,
    @param:RestClient private val gifDownloadClient: GifDownloadClient,
) {

    suspend fun searchGif(query: String): Gif {
        Log.info("Searching Giphy for: $query")
        val gifResult = this.restClient.search(query)

        val originalGif = gifResult.data?.first()?.images?.original ?: throw NoGifFoundException(query)

        val gifUrl = originalGif.url ?: throw NoGifFoundException(query)
        val height = originalGif.height?.toInt() ?: 0
        val width = originalGif.width?.toInt() ?: 0

        // NB : l'inversion width/height reproduit le comportement historique
        return Gif(gifDownloadClient.download(gifUrl), height, width)
    }
}
