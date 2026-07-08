package org.hoohoot.homelab.manager.portal.resource

import io.quarkus.logging.Log
import io.quarkus.security.identity.SecurityIdentity
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
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.portal.corrector.AnnotatedRelease
import org.hoohoot.homelab.manager.portal.corrector.CorrectorService
import org.hoohoot.homelab.manager.portal.persistence.CorrectorWorkflowEntity
import org.hoohoot.homelab.manager.portal.persistence.CorrectorWorkflowRepository
import org.hoohoot.homelab.manager.portal.persistence.GrabbedRelease
import org.hoohoot.homelab.manager.portal.persistence.MovieSnapshot
import java.time.LocalDateTime
import java.util.UUID

data class CorrectorMovieDto(
    val radarrMovieId: Int,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
)

data class CorrectorReleaseDto(
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

data class CorrectorWorkflowDto(
    val id: UUID,
    val mediaType: String,
    val status: String,
    val currentStep: String,
    val problemType: String?,
    val movie: MovieSnapshot?,
    val grabbedRelease: GrabbedRelease?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
)

data class CreateWorkflowRequest(val mediaType: String?)
data class SelectMovieRequest(val radarrMovieId: Int?)
data class SelectProblemRequest(val problemType: String?)
data class GrabReleaseRequest(
    val guid: String?,
    val indexerId: Int?,
    val title: String?,
    val indexer: String?,
    val quality: String?,
    val size: Long?,
)

@Path("/api/corrector")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Corrector")
class CorrectorResource(
    private val identity: SecurityIdentity,
    private val correctorWorkflowRepository: CorrectorWorkflowRepository,
    private val correctorService: CorrectorService,
) {
    private val username: String get() = identity.principal.name

    @GET
    @Path("/workflows")
    suspend fun listWorkflows(): List<CorrectorWorkflowDto> =
        correctorWorkflowRepository.listForUser(username).map { it.toDto() }

    @POST
    @Path("/workflows")
    @APIResponseSchema(value = CorrectorWorkflowDto::class, responseCode = "201")
    suspend fun createWorkflow(request: CreateWorkflowRequest): Response {
        if (request.mediaType != CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE) {
            return badRequest("mediaType must be '${CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE}'")
        }
        val entity = CorrectorWorkflowEntity()
        entity.id = UUID.randomUUID()
        entity.username = username
        entity.mediaType = CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE
        entity.status = CorrectorWorkflowEntity.STATUS_IN_PROGRESS
        entity.createdAt = LocalDateTime.now()
        entity.updatedAt = LocalDateTime.now()
        val saved = correctorWorkflowRepository.save(entity)
        return Response.status(Response.Status.CREATED).entity(saved.toDto()).build()
    }

    @GET
    @Path("/workflows/{id}")
    suspend fun getWorkflow(@PathParam("id") id: UUID): CorrectorWorkflowDto =
        correctorWorkflowRepository.findForUser(id, username)?.toDto()
            ?: throw NotFoundException()

    @GET
    @Path("/movies")
    suspend fun searchMovies(@QueryParam("query") query: String?): List<CorrectorMovieDto> {
        if (query.isNullOrBlank() || query.trim().length < 2) return emptyList()
        return correctorService.searchLibrary(query).mapNotNull { it.toMovieDto() }
    }

    @POST
    @Path("/workflows/{id}/movie")
    @APIResponseSchema(value = CorrectorWorkflowDto::class, responseCode = "200")
    suspend fun selectMovie(@PathParam("id") id: UUID, request: SelectMovieRequest): Response {
        val radarrMovieId = request.radarrMovieId ?: return badRequest("radarrMovieId is required")
        val workflow = correctorWorkflowRepository.findForUser(id, username) ?: return notFound()
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS) {
            return conflict("workflow is not in progress")
        }
        val movie = correctorService.findMovie(radarrMovieId)
            ?: return badRequest("movie $radarrMovieId not found in Radarr library")

        val updated = correctorWorkflowRepository.update(id, username) { entity ->
            entity.radarrMovieId = radarrMovieId
            entity.movieTitle = movie.title
            entity.problemType = null
            entity.state = entity.state.copy(movie = movie.toSnapshot(), grabbedRelease = null)
        } ?: return notFound()
        return Response.ok(updated.toDto()).build()
    }

    @POST
    @Path("/workflows/{id}/problem")
    @APIResponseSchema(value = CorrectorWorkflowDto::class, responseCode = "200")
    suspend fun selectProblem(@PathParam("id") id: UUID, request: SelectProblemRequest): Response {
        if (request.problemType != CorrectorWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH) {
            return badRequest("problemType must be '${CorrectorWorkflowEntity.PROBLEM_VO_SHOULD_BE_FRENCH}'")
        }
        val workflow = correctorWorkflowRepository.findForUser(id, username) ?: return notFound()
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS) {
            return conflict("workflow is not in progress")
        }
        if (workflow.radarrMovieId == null) {
            return conflict("a movie must be selected first")
        }
        val updated = correctorWorkflowRepository.update(id, username) { entity ->
            entity.problemType = request.problemType
        } ?: return notFound()
        return Response.ok(updated.toDto()).build()
    }

    // Proxy live : les guids des releases périment vite, on ne persiste rien ici
    @GET
    @Path("/workflows/{id}/releases")
    suspend fun listReleases(@PathParam("id") id: UUID): List<CorrectorReleaseDto> {
        val workflow = correctorWorkflowRepository.findForUser(id, username) ?: throw NotFoundException()
        val movieId = workflow.radarrMovieId
            ?: throw WebApplicationException("a movie must be selected first", Response.Status.CONFLICT)
        if (workflow.problemType == null) {
            throw WebApplicationException("a problem must be selected first", Response.Status.CONFLICT)
        }
        return correctorService.searchReleases(movieId).mapNotNull { it.toReleaseDto() }
    }

    @POST
    @Path("/workflows/{id}/grab")
    @APIResponseSchema(value = CorrectorWorkflowDto::class, responseCode = "200")
    suspend fun grabRelease(@PathParam("id") id: UUID, request: GrabReleaseRequest): Response {
        val guid = request.guid ?: return badRequest("guid is required")
        val indexerId = request.indexerId ?: return badRequest("indexerId is required")
        val workflow = correctorWorkflowRepository.findForUser(id, username) ?: return notFound()
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS || workflow.problemType == null) {
            return conflict("workflow must be in progress with a problem selected")
        }

        try {
            correctorService.grabRelease(guid, indexerId)
        } catch (exception: Exception) {
            Log.error("El Corrector: grab failed for workflow $id", exception)
            return Response.status(Response.Status.BAD_GATEWAY)
                .entity(mapOf("error" to "Radarr a refusé le téléchargement, la release n'est peut-être plus disponible"))
                .build()
        }

        val updated = correctorWorkflowRepository.update(id, username) { entity ->
            entity.status = CorrectorWorkflowEntity.STATUS_AWAITING_IMPORT
            entity.grabbedAt = LocalDateTime.now()
            entity.state = entity.state.copy(
                grabbedRelease = GrabbedRelease(
                    guid = guid,
                    indexerId = indexerId,
                    indexer = request.indexer,
                    title = request.title,
                    quality = request.quality,
                    size = request.size,
                ),
            )
        } ?: return notFound()
        return Response.ok(updated.toDto()).build()
    }

    @POST
    @Path("/workflows/{id}/abandon")
    @APIResponseSchema(value = CorrectorWorkflowDto::class, responseCode = "200")
    suspend fun abandonWorkflow(@PathParam("id") id: UUID): Response {
        val workflow = correctorWorkflowRepository.findForUser(id, username) ?: return notFound()
        if (workflow.status == CorrectorWorkflowEntity.STATUS_COMPLETED) {
            return conflict("workflow is already completed")
        }
        val updated = correctorWorkflowRepository.update(id, username) { entity ->
            entity.status = CorrectorWorkflowEntity.STATUS_ABANDONED
        } ?: return notFound()
        return Response.ok(updated.toDto()).build()
    }

    private fun CorrectorWorkflowEntity.toDto() = CorrectorWorkflowDto(
        id = id!!,
        mediaType = mediaType,
        status = status,
        currentStep = currentStep(),
        problemType = problemType,
        movie = state.movie,
        grabbedRelease = state.grabbedRelease,
        createdAt = createdAt,
        updatedAt = updatedAt,
        completedAt = completedAt,
    )

    // L'étape courante est dérivée de l'état : reprendre un workflow = recharger son DTO
    private fun CorrectorWorkflowEntity.currentStep(): String = when {
        status == CorrectorWorkflowEntity.STATUS_ABANDONED -> "ABANDONED"
        status == CorrectorWorkflowEntity.STATUS_COMPLETED -> "COMPLETED"
        status == CorrectorWorkflowEntity.STATUS_AWAITING_IMPORT -> "AWAITING_IMPORT"
        radarrMovieId == null -> "SELECT_MOVIE"
        problemType == null -> "SELECT_PROBLEM"
        else -> "SELECT_RELEASE"
    }

    private fun RadarrMovie.toMovieDto(): CorrectorMovieDto? {
        val movieId = id ?: return null
        val movieTitle = title ?: return null
        return CorrectorMovieDto(
            radarrMovieId = movieId,
            title = movieTitle,
            year = year,
            posterUrl = images.firstOrNull { it.coverType == "poster" }?.remoteUrl,
        )
    }

    private fun RadarrMovie.toSnapshot() = MovieSnapshot(
        title = title,
        year = year,
        posterUrl = images.firstOrNull { it.coverType == "poster" }?.remoteUrl,
        overview = overview,
        currentQuality = movieFile?.quality?.quality?.name,
        currentLanguages = movieFile?.languages.orEmpty().mapNotNull { it.name },
    )

    private fun AnnotatedRelease.toReleaseDto(): CorrectorReleaseDto? {
        val releaseGuid = release.guid ?: return null
        val releaseIndexerId = release.indexerId ?: return null
        val releaseTitle = release.title ?: return null
        return CorrectorReleaseDto(
            guid = releaseGuid,
            indexerId = releaseIndexerId,
            indexer = release.indexer,
            title = releaseTitle,
            quality = release.quality?.quality?.name,
            size = release.size,
            age = release.age,
            seeders = release.seeders,
            leechers = release.leechers,
            protocol = release.protocol,
            rejected = release.rejected == true,
            rejections = release.rejections,
            isFrench = isFrench,
        )
    }

    private fun badRequest(message: String) =
        Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to message)).build()

    private fun conflict(message: String) =
        Response.status(Response.Status.CONFLICT).entity(mapOf("error" to message)).build()

    private fun notFound() = Response.status(Response.Status.NOT_FOUND).build()
}
