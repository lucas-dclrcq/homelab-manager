package org.hoohoot.homelab.manager.infrastructure.arr

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Episode

@Path("/api/v3")
@RegisterRestClient(configKey = "sonarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${sonarr.api_key}"])
interface SonarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
        @QueryParam("includeSeries") includeSeries: Boolean?
    ): List<Episode>?
}