package org.hoohoot.homelab.manager.cleanup.api

import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.cleanup.domain.Accessor
import org.hoohoot.homelab.manager.cleanup.domain.VetoChannel
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import org.hoohoot.homelab.manager.cleanup.domain.usecases.GetCampaignOverview
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ProtectMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ProtectionRequest
import org.hoohoot.homelab.manager.cleanup.domain.usecases.SearchProtectableMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.UnprotectMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.VetoCandidate
import java.util.UUID

@Path("/api/cleanup")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Cleanup")
class CleanupResource(
    private val identity: SecurityIdentity,
    private val getCampaignOverview: GetCampaignOverview,
    private val vetoCandidate: VetoCandidate,
    private val protections: Protections,
    private val protectMedia: ProtectMedia,
    private val unprotectMedia: UnprotectMedia,
    private val searchProtectableMedia: SearchProtectableMedia,
) {
    private val username: String get() = identity.principal.name

    @GET
    @Path("/campaign")
    suspend fun getCampaign(): CleanupOverviewDto {
        val overview = getCampaignOverview()
        return CleanupOverviewDto(
            campaign = overview.campaign?.toDetailsDto(overview.candidates),
            diskPath = overview.config.diskPath,
            diskFreeBytes = overview.diskFreeBytes,
            thresholdBytes = overview.config.thresholdFreeBytes,
            targetFreeBytes = overview.config.targetFreeBytes,
        )
    }

    @POST
    @Path("/candidates/{id}/veto")
    @APIResponseSchema(value = CleanupCandidateDto::class, responseCode = "200")
    suspend fun veto(@PathParam("id") id: UUID): Response =
        vetoCandidate(id, username, VetoChannel.WEB).toResponse()

    @GET
    @Path("/protections")
    suspend fun listProtections(): List<CleanupProtectionDto> =
        protections.all().map { it.toDto() }

    @POST
    @Path("/protections")
    @APIResponseSchema(value = CleanupProtectionDto::class, responseCode = "201")
    suspend fun protect(@Valid request: CreateProtectionRequest): Response =
        protectMedia(
            username,
            ProtectionRequest(
                mediaKind = requireNotNull(request.mediaKind),
                radarrMovieId = request.radarrMovieId,
                sonarrSeriesId = request.sonarrSeriesId,
                seasonNumber = request.seasonNumber,
            ),
        ).toResponse()

    @DELETE
    @Path("/protections/{id}")
    suspend fun unprotect(@PathParam("id") id: UUID): Response =
        unprotectMedia(id, Accessor.User(username)).toResponse()

    @GET
    @Path("/search/movies")
    suspend fun searchMovies(@QueryParam("query") query: String?): List<CleanupMediaDto> {
        if (query.isNullOrBlank() || query.trim().length < 2) return emptyList()
        return searchProtectableMedia.movies(query).map { it.toMediaDto() }
    }

    @GET
    @Path("/search/series")
    suspend fun searchSeries(@QueryParam("query") query: String?): List<CleanupMediaDto> {
        if (query.isNullOrBlank() || query.trim().length < 2) return emptyList()
        return searchProtectableMedia.series(query).map { it.toMediaDto() }
    }
}
