package org.hoohoot.homelab.manager.notifications.arr.radarr

import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.notifications.arr.DiskSpace

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

    @GET
    @Path("/movie")
    suspend fun getMovies(): List<RadarrMovie>?

    @GET
    @Path("/history/since")
    suspend fun getHistorySince(
        @QueryParam("date") date: String,
        @QueryParam("includeMovie") includeMovie: Boolean,
    ): List<RadarrHistoryRecord>?

    @GET
    @Path("/diskspace")
    suspend fun getDiskSpace(): List<DiskSpace>?

    @GET
    @Path("/release")
    suspend fun searchReleases(@QueryParam("movieId") movieId: Int): List<RadarrRelease>?

    @POST
    @Path("/release")
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun grabRelease(request: RadarrGrabRequest): RadarrRelease?
}

suspend fun RadarrRestClient.getMovieCalendar(start: Instant, end: Instant): List<RadarrMovie> =
    getCalendar(
        start.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        end.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
    ) ?: emptyList()
