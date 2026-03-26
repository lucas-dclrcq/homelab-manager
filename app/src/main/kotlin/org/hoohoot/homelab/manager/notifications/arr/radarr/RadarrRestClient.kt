package org.hoohoot.homelab.manager.notifications.arr.radarr

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api/v3")
@RegisterRestClient(configKey = "radarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${radarr.api_key}"])
interface RadarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
    ): List<RadarrMovie>?
}

suspend fun RadarrRestClient.getMovieCalendar(start: Instant, end: Instant): List<RadarrMovie> =
    getCalendar(
        start.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        end.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
    ) ?: emptyList()
