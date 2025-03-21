package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.Operation
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.*

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Random", description = "Miscellaneous endpoints")
class RandomResource(private val mediator: Mediator) {
    @GET
    @Path("/skong/{skongType}")
    @Operation(summary = "Get a SKONG. Whereas you're a beleiver or a doubter")
    suspend fun skong(
        @Parameter(description = "The type of SKONG you want", example = "beleiver")
        @PathParam("skongType") skongType: String
    ): SkongResponse {
        val skong = when (skongType) {
            "believer", "beleiver" -> SkongType.Believer
            "doubter" -> SkongType.Doubter
            else -> throw IllegalArgumentException("Unsupported skong: $skongType")
        }

        return this.mediator.send(GetSkong(skong))
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Test the server is working")
    suspend fun ping() = this.mediator.send(Ping)

    @GET
    @Path("/gif")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Operation(summary = "Get a random GIF relative to the given query")
    suspend fun getGif(
        @Parameter(description = "The term you want to find a GIF for", example = "dog")
        @QueryParam("query") query: String
    ): Response {
        this.mediator.send(GetGif(query))
            .let { return Response.ok(it).build() }
    }
}