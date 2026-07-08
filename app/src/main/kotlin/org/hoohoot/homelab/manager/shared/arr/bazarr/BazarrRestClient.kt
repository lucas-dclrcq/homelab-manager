package org.hoohoot.homelab.manager.shared.arr.bazarr

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api")
@RegisterRestClient(configKey = "bazarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-API-KEY", value = ["\${bazarr.api_key}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface BazarrRestClient {
    @GET
    @Path("/episodes/history")
    suspend fun getEpisodesHistory(
        @QueryParam("start") start: Int,
        @QueryParam("length") length: Int,
    ): BazarrHistoryPage?

    @GET
    @Path("/movies/history")
    suspend fun getMoviesHistory(
        @QueryParam("start") start: Int,
        @QueryParam("length") length: Int,
    ): BazarrHistoryPage?
}
