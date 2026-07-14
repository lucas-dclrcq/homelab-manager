package org.hoohoot.homelab.manager.shared.arr.lidarr

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
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@Path("/api/v1")
@RegisterRestClient(configKey = "lidarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${lidarr.api_key}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface LidarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
        @QueryParam("includeArtist") includeArtist: Boolean?
    ): List<LidarrAlbum>?

    @GET
    @Path("/history/since")
    suspend fun getHistorySince(
        @QueryParam("date") date: String,
        @QueryParam("includeAlbum") includeAlbum: Boolean,
        @QueryParam("includeArtist") includeArtist: Boolean,
    ): List<LidarrHistoryRecord>?
}

suspend fun LidarrRestClient.getAlbumCalendar(start: Instant, end: Instant): List<LidarrAlbum> =
    getCalendar(
        start.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        end.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        true
    ) ?: emptyList()
