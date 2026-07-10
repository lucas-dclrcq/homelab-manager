package org.hoohoot.homelab.manager.problems.api

import jakarta.annotation.security.RolesAllowed
import jakarta.validation.Valid
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.usecases.AbandonWorkflow
import org.hoohoot.homelab.manager.problems.domain.usecases.DeleteWorkflow
import org.hoohoot.homelab.manager.problems.domain.usecases.GrabRelease
import org.hoohoot.homelab.manager.problems.domain.usecases.GrabRequest
import org.hoohoot.homelab.manager.problems.domain.usecases.ListReleases
import org.hoohoot.homelab.manager.problems.domain.usecases.ListReleasesResult
import org.hoohoot.homelab.manager.problems.domain.usecases.ResolveWorkflow
import org.hoohoot.homelab.manager.problems.domain.usecases.SelectMovie
import org.hoohoot.homelab.manager.problems.domain.usecases.SelectProblem
import org.hoohoot.homelab.manager.problems.domain.usecases.SelectSeries
import org.hoohoot.homelab.manager.shared.api.conflict
import java.util.UUID

// Reprise d'un problème par un admin : mêmes use cases que la resource user,
// mais avec Accessor.Admin (accès à tous les workflows, sans réassignation)
@Path("/api/admin/problems")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name = "Problems")
class AdminProblemsResource(
    private val workflows: ProblemWorkflows,
    private val selectMovieUseCase: SelectMovie,
    private val selectSeriesUseCase: SelectSeries,
    private val selectProblemUseCase: SelectProblem,
    private val listReleasesUseCase: ListReleases,
    private val grabReleaseUseCase: GrabRelease,
    private val resolveWorkflowUseCase: ResolveWorkflow,
    private val abandonWorkflowUseCase: AbandonWorkflow,
    private val deleteWorkflowUseCase: DeleteWorkflow,
) {
    private val accessor: Accessor = Accessor.Admin

    @GET
    @Path("/workflows")
    suspend fun listWorkflows(): List<AdminProblemWorkflowDto> =
        workflows.listAll().map { it.toAdminDto() }

    @GET
    @Path("/workflows/{id}")
    suspend fun getWorkflow(@PathParam("id") id: UUID): AdminProblemWorkflowDto =
        workflows.find(id, accessor)?.toAdminDto()
            ?: throw NotFoundException()

    @POST
    @Path("/workflows/{id}/movie")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun selectMovie(@PathParam("id") id: UUID, @Valid request: SelectMovieRequest): Response =
        selectMovieUseCase(id, accessor, requireNotNull(request.radarrMovieId)).toResponse()

    @POST
    @Path("/workflows/{id}/series")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun selectSeries(@PathParam("id") id: UUID, @Valid request: SelectSeriesRequest): Response =
        selectSeriesUseCase(id, accessor, requireNotNull(request.sonarrSeriesId)).toResponse()

    @POST
    @Path("/workflows/{id}/problem")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun selectProblem(@PathParam("id") id: UUID, request: SelectProblemRequest): Response =
        selectProblemUseCase(id, accessor, request.problemType, request.description).toResponse()

    @GET
    @Path("/workflows/{id}/releases")
    suspend fun listReleases(@PathParam("id") id: UUID): List<ProblemReleaseDto> =
        when (val result = listReleasesUseCase(id, accessor)) {
            is ListReleasesResult.Ok -> result.releases.map { it.toReleaseDto() }
            is ListReleasesResult.NotFound -> throw NotFoundException()
            is ListReleasesResult.Conflict -> throw WebApplicationException(conflict(result.message))
        }

    @POST
    @Path("/workflows/{id}/grab")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun grabRelease(@PathParam("id") id: UUID, @Valid request: GrabReleaseRequest): Response =
        grabReleaseUseCase(
            id,
            accessor,
            GrabRequest(
                guid = requireNotNull(request.guid),
                indexerId = requireNotNull(request.indexerId),
                title = request.title,
                indexer = request.indexer,
                quality = request.quality,
                size = request.size,
            ),
        ).toResponse()

    @POST
    @Path("/workflows/{id}/resolve")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun resolveWorkflow(@PathParam("id") id: UUID): Response =
        resolveWorkflowUseCase(id, accessor).toResponse()

    @POST
    @Path("/workflows/{id}/abandon")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun abandonWorkflow(@PathParam("id") id: UUID): Response =
        abandonWorkflowUseCase(id, accessor).toResponse()

    @DELETE
    @Path("/workflows/{id}")
    @APIResponse(responseCode = "204", description = "Workflow supprimé")
    suspend fun deleteWorkflow(@PathParam("id") id: UUID): Response =
        if (deleteWorkflowUseCase(id)) Response.noContent().build()
        else throw NotFoundException()
}
