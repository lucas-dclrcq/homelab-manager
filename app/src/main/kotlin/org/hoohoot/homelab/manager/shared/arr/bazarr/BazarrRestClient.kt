package org.hoohoot.homelab.manager.shared.arr.bazarr

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
