package org.hoohoot.homelab.manager.infrastructure.api.resources

import com.trendyol.kediatr.Mediator
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.application.queries.GetSkong
import org.hoohoot.homelab.manager.application.queries.SkongResponse
import org.hoohoot.homelab.manager.application.queries.SkongType

@Path("/api/skong")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Media Info")
class RandomResource(private val mediator: Mediator) {
    @GET
    @Path("{skongType}")
    suspend fun skong(@PathParam("skongType") skongType: String): SkongResponse {
        val skong = when (skongType) {
            "believer" -> SkongType.Believer
            "doubter" -> SkongType.Doubter
            else -> throw IllegalArgumentException("Unsupported skong: $skongType")
        }

        return this.mediator.send(GetSkong(skong))
    }
}