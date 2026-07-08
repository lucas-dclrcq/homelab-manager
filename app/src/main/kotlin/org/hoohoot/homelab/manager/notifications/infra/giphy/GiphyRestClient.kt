package org.hoohoot.homelab.manager.notifications.infra.giphy

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import io.quarkus.rest.client.reactive.ClientQueryParam
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/v1/gifs")
@RegisterRestClient(configKey = "giphy-api")
@ClientQueryParam(name = "api_key", value = ["\${giphy.api_key}"])
@ClientQueryParam(name = "rating", value = ["\${giphy.rating}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface GiphyRestClient {
    @GET
    @Path("/search")
    suspend fun search(@QueryParam("q") query: String): GiphyResponse
}

data class GiphyResponse(val data: List<GiphyData>? = null)

data class GiphyData(val images: GiphyImages? = null)

data class GiphyImages(val original: GiphyImageDetails? = null)

data class GiphyImageDetails(val url: String? = null, val width: String? = null, val height: String? = null)
