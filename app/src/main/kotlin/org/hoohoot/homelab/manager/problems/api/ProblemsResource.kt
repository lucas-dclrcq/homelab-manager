package org.hoohoot.homelab.manager.problems.api

import io.quarkus.security.identity.SecurityIdentity
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.problems.domain.AnnotatedRelease
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.LibraryMovie
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.usecases.AbandonWorkflow
import org.hoohoot.homelab.manager.problems.domain.usecases.CreateWorkflow
import org.hoohoot.homelab.manager.problems.domain.usecases.GrabRelease
import org.hoohoot.homelab.manager.problems.domain.usecases.GrabRequest
import org.hoohoot.homelab.manager.problems.domain.usecases.ListReleases
import org.hoohoot.homelab.manager.problems.domain.usecases.ListReleasesResult
import org.hoohoot.homelab.manager.problems.domain.usecases.SearchLibrary
import org.hoohoot.homelab.manager.problems.domain.usecases.SelectMovie
import org.hoohoot.homelab.manager.problems.domain.usecases.SelectProblem
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.problems.infra.GrabbedRelease
import org.hoohoot.homelab.manager.problems.infra.MediaSnapshot
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.api.notFound
import java.time.LocalDateTime
import java.util.UUID

data class ProblemMovieDto(
    val radarrMovieId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
)

data class ProblemReleaseDto(
    val guid: String,
    val indexerId: Int,
    val indexer: String?,
    val title: String,
    val quality: String?,
    val size: Long?,
    val age: Int?,
    val seeders: Int?,
    val leechers: Int?,
    val protocol: String?,
    val rejected: Boolean,
    val rejections: List<String>,
    val isFrench: Boolean,
)

data class ProblemWorkflowDto(
    val id: UUID,
    val mediaType: String,
    val status: String,
    val currentStep: String,
    val problemType: String?,
    val media: MediaSnapshot?,
    val grabbedRelease: GrabbedRelease?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
)

data class CreateWorkflowRequest(val mediaType: String?)
data class SelectMovieRequest(@field:NotNull val radarrMovieId: Int?)
data class SelectProblemRequest(val problemType: String?)
data class GrabReleaseRequest(
    @field:NotNull val guid: String?,
    @field:NotNull val indexerId: Int?,
    val title: String?,
    val indexer: String?,
    val quality: String?,
    val size: Long?,
)

@Path("/api/problems")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Problems")
class ProblemsResource(
    private val identity: SecurityIdentity,
    private val workflows: ProblemWorkflows,
    private val searchLibrary: SearchLibrary,
    private val createWorkflowUseCase: CreateWorkflow,
    private val selectMovieUseCase: SelectMovie,
    private val selectProblemUseCase: SelectProblem,
    private val listReleasesUseCase: ListReleases,
    private val grabReleaseUseCase: GrabRelease,
    private val abandonWorkflowUseCase: AbandonWorkflow,
) {
    companion object {
        private const val GRAB_FAILED_MESSAGE =
            "Radarr a refusé le téléchargement, la release n'est peut-être plus disponible"
    }

    private val username: String get() = identity.principal.name

    @GET
    @Path("/workflows")
    suspend fun listWorkflows(): List<ProblemWorkflowDto> =
        workflows.listForUser(username).map { it.toDto() }

    @POST
    @Path("/workflows")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "201")
    suspend fun createWorkflow(request: CreateWorkflowRequest): Response =
        createWorkflowUseCase(username, request.mediaType).toResponse(Response.Status.CREATED)

    @GET
    @Path("/workflows/{id}")
    suspend fun getWorkflow(@PathParam("id") id: UUID): ProblemWorkflowDto =
        workflows.findForUser(id, username)?.toDto()
            ?: throw NotFoundException()

    @GET
    @Path("/movies")
    suspend fun searchMovies(@QueryParam("query") query: String?): List<ProblemMovieDto> {
        if (query.isNullOrBlank() || query.trim().length < 2) return emptyList()
        return searchLibrary(query).map { it.toMovieDto() }
    }

    @POST
    @Path("/workflows/{id}/movie")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun selectMovie(@PathParam("id") id: UUID, @Valid request: SelectMovieRequest): Response =
        selectMovieUseCase(id, username, requireNotNull(request.radarrMovieId)).toResponse()

    @POST
    @Path("/workflows/{id}/problem")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun selectProblem(@PathParam("id") id: UUID, request: SelectProblemRequest): Response =
        selectProblemUseCase(id, username, request.problemType).toResponse()

    @GET
    @Path("/workflows/{id}/releases")
    suspend fun listReleases(@PathParam("id") id: UUID): List<ProblemReleaseDto> =
        when (val result = listReleasesUseCase(id, username)) {
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
            username,
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
    @Path("/workflows/{id}/abandon")
    @APIResponseSchema(value = ProblemWorkflowDto::class, responseCode = "200")
    suspend fun abandonWorkflow(@PathParam("id") id: UUID): Response =
        abandonWorkflowUseCase(id, username).toResponse()

    private fun ProblemResult.toResponse(okStatus: Response.Status = Response.Status.OK): Response = when (this) {
        is ProblemResult.Ok -> Response.status(okStatus).entity(workflow.toDto()).build()
        ProblemResult.NotFound -> notFound()
        is ProblemResult.Invalid -> badRequest(message)
        is ProblemResult.Conflict -> conflict(message)
        ProblemResult.GrabFailed ->
            Response.status(Response.Status.BAD_GATEWAY).entity(mapOf("error" to GRAB_FAILED_MESSAGE)).build()
    }

    private fun ProblemWorkflowEntity.toDto() = ProblemWorkflowDto(
        id = requireNotNull(id),
        mediaType = mediaType,
        status = status,
        currentStep = currentStep(),
        problemType = problemType,
        media = state.media,
        grabbedRelease = state.grabbedRelease,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
    )

    // L'étape courante est dérivée de l'état : reprendre un workflow = recharger son DTO
    private fun ProblemWorkflowEntity.currentStep(): String = when {
        status == ProblemWorkflowEntity.STATUS_ABANDONED -> "ABANDONED"
        status == ProblemWorkflowEntity.STATUS_COMPLETED -> "COMPLETED"
        status == ProblemWorkflowEntity.STATUS_AWAITING_IMPORT -> "AWAITING_IMPORT"
        radarrMovieId == null -> "SELECT_MOVIE"
        problemType == null -> "SELECT_PROBLEM"
        else -> "SELECT_RELEASE"
    }

    private fun LibraryMovie.toMovieDto() = ProblemMovieDto(
        radarrMovieId = radarrMovieId,
        title = title,
        year = year,
        posterUrl = posterUrl,
    )

    private fun AnnotatedRelease.toReleaseDto() = ProblemReleaseDto(
        guid = release.guid,
        indexerId = release.indexerId,
        indexer = release.indexer,
        title = release.title,
        quality = release.quality,
        size = release.size,
        age = release.age,
        seeders = release.seeders,
        leechers = release.leechers,
        protocol = release.protocol,
        rejected = release.rejected,
        rejections = release.rejections,
        isFrench = isFrench,
    )
}
