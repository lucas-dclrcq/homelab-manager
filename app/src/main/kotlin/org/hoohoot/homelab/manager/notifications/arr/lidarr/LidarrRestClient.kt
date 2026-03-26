package org.hoohoot.homelab.manager.notifications.arr.lidarr

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

@Path("/api/v1")
@RegisterRestClient(configKey = "lidarr-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "X-Api-Key", value = ["\${lidarr.api_key}"])
interface LidarrRestClient {
    @GET
    @Path("/calendar")
    suspend fun getCalendar(
        @QueryParam("start") start: String?,
        @QueryParam("end") end: String?,
        @QueryParam("includeArtist") includeArtist: Boolean?
    ): List<LidarrAlbum>?
}

suspend fun LidarrRestClient.getAlbumCalendar(start: Instant, end: Instant): List<LidarrAlbum> =
    getCalendar(
        start.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        end.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET),
        true
    ) ?: emptyList()
