package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import jakarta.validation.constraints.NotBlank
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.*

@Path("/api/media-info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Media Info", description = "Informations about the homelab Media Center")
class MediaInfoResource(private val mediator: Mediator) {
    @GET
    @Path("/who-watched")
    @Operation(summary = "Find out who watched a tv show")
    suspend fun whoWatched(
        @Parameter(description = "The term you want to find watched information for", example = "Breaking Bad")
        @QueryParam("searchTerm")
        @NotBlank searchTerm: String
    ): WhoWatchedInfos =
        this.mediator.send(WhoWatched(searchTerm))

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

        return this.mediator.send(GetTopWatched(topWatchedPeriod))
    }

    @GET
    @Path("/top-watchers")
    @Operation(summary = "Find out who's watching the most")
    suspend fun topWatchers(
        @Parameter(description = "The maximum number of watchers to return", example = "10", required = false)
        @QueryParam("limit") limit: Int?
    ): List<UserStatistics> =
        this.mediator.send(GetTopWatchers(limit ?: 10))
}