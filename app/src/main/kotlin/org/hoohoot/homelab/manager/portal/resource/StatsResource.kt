package org.hoohoot.homelab.manager.portal.resource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.portal.stats.LibraryStats
import org.hoohoot.homelab.manager.portal.stats.StatsService

@Path("/api/stats")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portal")
class StatsResource(private val statsService: StatsService) {

    @GET
    suspend fun getStats(): LibraryStats = statsService.collectStats()
}
