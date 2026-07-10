package org.hoohoot.homelab.manager.shared.jellyfin

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import io.quarkus.rest.client.reactive.ClientQueryParam
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "jellyfin-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ClientQueryParam(name = "ApiKey", value = ["\${jellyfin.api_key}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface JellyfinRestClient {
    @GET
    @Path("/Search/Hints")
    suspend fun getSearchHintResult(
        @QueryParam("searchTerm") searchTerm: String,
        @QueryParam("includeItemTypes") includeItemTypes: String?
    ): JellyfinSearchHintsResponse

    @GET
    @Path("/Sessions")
    suspend fun getSessions(): List<JellyfinSessionDto>
}

suspend fun JellyfinRestClient.searchSeries(searchTerm: String): List<MediaSearchResult> =
    getSearchHintResult(searchTerm, "Series")
        .searchHints
        ?.map { MediaSearchResult(it.itemId ?: "", it.name ?: "", it.type ?: "") }
        ?: emptyList()
