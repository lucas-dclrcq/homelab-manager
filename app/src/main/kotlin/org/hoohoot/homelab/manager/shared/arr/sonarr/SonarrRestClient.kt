package org.hoohoot.homelab.manager.shared.arr.sonarr

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
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
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
}

suspend fun SonarrRestClient.getSeriesCalendar(start: Instant, end: Instant): List<Episode> =
    getCalendar(
        start.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        end.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        true
    ) ?: emptyList()
