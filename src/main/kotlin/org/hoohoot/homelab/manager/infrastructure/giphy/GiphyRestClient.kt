package org.hoohoot.homelab.manager.infrastructure.giphy

import io.quarkus.rest.client.reactive.ClientQueryParam
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/v1/gifs")
@RegisterRestClient(configKey = "giphy-api")
@ClientQueryParam(name = "api_key", value = ["\${giphy.api_key}"])
@ClientQueryParam(name = "rating", value = ["\${giphy.rating}"])
interface GiphyRestClient {
    @GET
    @Path("/search")
    suspend fun search(@QueryParam("q") query: String): GiphyResponse
}

data class GiphyResponse(val data: List<GiphyData>? = null)

data class GiphyData(val images: GiphyImages? = null)

data class GiphyImages(val original: GiphyImageDetails? = null)

data class GiphyImageDetails(val url: String? = null, val width: String? = null, val height: String? = null)
