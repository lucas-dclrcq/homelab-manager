package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.WhoWatched
import org.hoohoot.homelab.manager.application.queries.WhoWatchedInfos

@Path("/api/media-info")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Media Info")
class MediaInfoResource(private val mediator: Mediator) {
    @GET
    @Path("/who-watched")
    suspend fun whoWatched(@QueryParam("searchTerm") searchTerm: String): WhoWatchedInfos =
        this.mediator.send(WhoWatched(searchTerm))
}