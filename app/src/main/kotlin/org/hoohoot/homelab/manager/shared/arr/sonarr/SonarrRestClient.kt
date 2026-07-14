package org.hoohoot.homelab.manager.shared.arr.sonarr

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.shared.arr.ArrTag
import org.hoohoot.homelab.manager.shared.arr.DiskSpace

@Path("/api/v3")
@RegisterRestClient(configKey = "sonarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${sonarr.api_key}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface SonarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
        @QueryParam("includeSeries") includeSeries: Boolean?
    ): List<Episode>?

    @GET
    @Path("/series")
    suspend fun getSeries(): List<Series>?

    @GET
    @Path("/history/since")
    suspend fun getHistorySince(
        @QueryParam("date") date: String,
        @QueryParam("includeSeries") includeSeries: Boolean,
        @QueryParam("includeEpisode") includeEpisode: Boolean,
    ): List<SonarrHistoryRecord>?

    @GET
    @Path("/diskspace")
    suspend fun getDiskSpace(): List<DiskSpace>?

    @GET
    @Path("/tag")
    suspend fun getTags(): List<ArrTag>?

    @GET
    @Path("/series/{id}")
    suspend fun getSeriesById(@PathParam("id") id: Int): Series?

    @GET
    @Path("/episodefile")
    suspend fun getEpisodeFiles(@QueryParam("seriesId") seriesId: Int): List<SonarrEpisodeFile>?

    @DELETE
    @Path("/episodefile/{id}")
    suspend fun deleteEpisodeFile(@PathParam("id") id: Int)

    @DELETE
    @Path("/series/{id}")
    suspend fun deleteSeries(
        @PathParam("id") id: Int,
        @QueryParam("deleteFiles") deleteFiles: Boolean,
        @QueryParam("addImportListExclusion") addImportListExclusion: Boolean,
    )

    @PUT
    @Path("/series/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun updateSeries(@PathParam("id") id: Int, series: Series): Series?
}

suspend fun SonarrRestClient.getSeriesCalendar(start: Instant, end: Instant): List<Episode> =
    getCalendar(
        start.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        end.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        true
    ) ?: emptyList()
