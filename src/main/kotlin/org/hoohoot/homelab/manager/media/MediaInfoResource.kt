package org.hoohoot.homelab.manager.media

import io.quarkus.logging.Log
import jakarta.validation.constraints.NotBlank
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.jellyfin.JellyfinRestClient
import org.hoohoot.homelab.manager.jellyfin.searchSeries
import org.hoohoot.homelab.manager.jellystat.JellystatService

@Path("/api/media-info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Media Info", description = "Informations about the homelab Media Center")
class MediaInfoResource(
    private val jellystatService: JellystatService,
    @RestClient private val jellyfinRestClient: JellyfinRestClient
) {
    @GET
    @Path("/who-watched")
    @Operation(summary = "Find out who watched a tv show")
    suspend fun whoWatched(
        @Parameter(description = "The term you want to find watched information for", example = "Breaking Bad")
        @QueryParam("searchTerm")
        @NotBlank searchTerm: String
    ): WhoWatchedInfos {
        Log.info("Finding out who watched $searchTerm")

        val media = jellyfinRestClient.searchSeries(searchTerm)

        if (media.isEmpty()) {
            throw NoSeriesFoundException("No series found for '$searchTerm'")
        }

        if (media.size > 1) {
            throw MultipleSeriesFoundException("Multiple series found for '$searchTerm' : ${media.joinToString(",") { it.name }}. Please be more specific.")
        }

        val firstFoundMedia = media.first()
        return jellystatService.getWatchersInfo(firstFoundMedia.itemId, firstFoundMedia.name)
    }

    @GET
    @Path("/top-watched/{period}")
    @Operation(summary = "Find out what's been watched the most in the last period")
    suspend fun topWatched(
        @Parameter(description = "The period to look back in", example = "last-week")
        @NotBlank
        @PathParam("period") period: String
    ): TopWatched {
        val topWatchedPeriod = when (period) {
            "last-week" -> TopWatchedPeriod.LastWeek
            "last-month" -> TopWatchedPeriod.LastMonth
            "last-year" -> TopWatchedPeriod.LastYear
            else -> throw IllegalArgumentException("Unsupported period: $period")
        }

        return jellystatService.getTopWatched(topWatchedPeriod)
    }

    @GET
    @Path("/top-watchers")
    @Operation(summary = "Find out who's watching the most")
    suspend fun topWatchers(
        @Parameter(description = "The maximum number of watchers to return", example = "10", required = false)
        @QueryParam("limit") limit: Int?
    ): List<UserStatistics> =
        jellystatService.getTopWatchers(limit ?: 10)
}
