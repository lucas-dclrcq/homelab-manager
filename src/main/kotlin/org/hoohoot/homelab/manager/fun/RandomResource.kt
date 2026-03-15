package org.hoohoot.homelab.manager.`fun`

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.datetime.LocalDate
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.giphy.GiphyService
import org.hoohoot.homelab.manager.time.TimeService

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Random", description = "Miscellaneous endpoints")
class RandomResource(
    private val giphyService: GiphyService,
    private val timeService: TimeService
) {
    private val skongOrigin = LocalDate.parse("2019-02-14")

    @GET
    @Path("/skong/{skongType}")
    @Operation(summary = "Get a SKONG. Whereas you're a beleiver or a doubter")
    suspend fun skong(
        @Parameter(description = "The type of SKONG you want", example = "beleiver")
        @PathParam("skongType") skongType: String
    ): SkongResponse {
        val daysSince = timeService.getDaysSince(skongOrigin)

        val message = when (skongType) {
            "believer", "beleiver" -> """🟢 Patience, my child. Silksong will come when it is ready. The longer the wait, the greater the masterpiece!"""
            "doubter" -> """🔴 It's been $daysSince days, and there is still no release date. Face it, Silksong is never coming out. Team Cherry is just a myth."""
            else -> throw IllegalArgumentException("Unsupported skong: $skongType")
        }

        return SkongResponse(message)
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Test the server is working")
    suspend fun ping() = "Pong!"

    @GET
    @Path("/gif")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Get a random GIF relative to the given query")
    suspend fun getGif(
        @Parameter(description = "The term you want to find a GIF for", example = "dog")
        @QueryParam("query") query: String
    ): Response {
        giphyService.searchGif(query)
            .let { return Response.ok(it).build() }
    }
}
