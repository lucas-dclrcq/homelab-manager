package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import jakarta.validation.constraints.NotBlank
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.*

@Path("/api/media-info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Media Info")
class MediaInfoResource(private val mediator: Mediator) {
    @GET
    @Path("/who-watched")
    suspend fun whoWatched(@QueryParam("searchTerm") @NotBlank searchTerm: String): WhoWatchedInfos =
        this.mediator.send(WhoWatched(searchTerm))

    @GET
    @Path("/top-watched/{period}")
    suspend fun topWatched(@PathParam("period") period: String): TopWatched {
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
    suspend fun topWatchers(@QueryParam("limit") limit: Int?): List<UserStatistics> =
        this.mediator.send(GetTopWatchers(limit ?: 10))
}