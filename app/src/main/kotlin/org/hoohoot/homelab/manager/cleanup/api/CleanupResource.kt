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
import org.hoohoot.homelab.manager.cleanup.domain.ports.Suggestions
import org.hoohoot.homelab.manager.cleanup.domain.usecases.GetCampaignOverview
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ProtectMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ProtectionRequest
import org.hoohoot.homelab.manager.cleanup.domain.usecases.SearchProtectableMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.SuggestDeletion
import org.hoohoot.homelab.manager.cleanup.domain.usecases.SuggestionRequest
import org.hoohoot.homelab.manager.cleanup.domain.usecases.UnprotectMedia
import org.hoohoot.homelab.manager.cleanup.domain.usecases.VetoCandidate
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import java.time.LocalDateTime
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
    private val suggestions: Suggestions,
    private val suggestDeletion: SuggestDeletion,
) {
    companion object {
        // Les issues récentes (veto, suppression…) restent visibles quelques jours dans l'UI
        private const val RECENT_SUGGESTIONS_DAYS = 7L
        private const val DEFAULT_PROTECTIONS_PAGE_SIZE = 8
        private const val MAX_PROTECTIONS_PAGE_SIZE = 100
    }

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
    suspend fun listProtections(
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?,
    ): CleanupProtectionsPageDto =
        protections.page(
            page = (page ?: 0).coerceAtLeast(0),
            pageSize = (pageSize ?: DEFAULT_PROTECTIONS_PAGE_SIZE).coerceIn(1, MAX_PROTECTIONS_PAGE_SIZE),
        ).toDto()

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
    @Path("/suggestions")
    suspend fun listSuggestions(): List<CleanupSuggestionDto> =
        suggestions.listRecent(LocalDateTime.now().minusDays(RECENT_SUGGESTIONS_DAYS))
            .sortedWith(
                compareBy<CleanupSuggestionEntity> { it.status != CleanupSuggestionEntity.STATUS_PENDING }
                    .thenBy { it.deleteAfter },
            )
            .map { it.toDto() }

    @POST
    @Path("/suggestions")
    @APIResponseSchema(value = CleanupSuggestionDto::class, responseCode = "201")
    suspend fun suggest(@Valid request: CreateSuggestionRequest): Response =
        suggestDeletion(
            username,
            SuggestionRequest(
                mediaKind = requireNotNull(request.mediaKind),
                radarrMovieId = request.radarrMovieId,
                sonarrSeriesId = request.sonarrSeriesId,
                seasonNumber = request.seasonNumber,
            ),
        ).toResponse()

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
