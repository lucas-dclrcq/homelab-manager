package org.hoohoot.homelab.manager.applications.api

import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.core.Vertx
import jakarta.annotation.security.RolesAllowed
import jakarta.validation.constraints.NotBlank
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import org.hoohoot.homelab.manager.applications.infra.ApplicationRepository
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.time.LocalDateTime
import java.util.UUID

data class ApplicationDto(
    val id: UUID,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val hasLogo: Boolean,
    val managedBy: String?,
    val externalId: String?,
    val updatedAt: LocalDateTime?,
)

private val ALLOWED_LOGO_CONTENT_TYPES = setOf("image/png", "image/jpeg", "image/svg+xml", "image/webp")
private const val MAX_LOGO_SIZE_BYTES = 1024L * 1024L

@Path("/api/applications")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portal")
class ApplicationsResource(
    private val applicationRepository: ApplicationRepository,
    private val vertx: Vertx,
) {

    @GET
    suspend fun listApplications(): List<ApplicationDto> =
        applicationRepository.listSummaries().map {
            ApplicationDto(it.id, it.name, it.category, it.description, it.url, it.requiresVpn, it.hasLogo, it.managedBy, it.externalId, it.updatedAt)
        }

    @POST
    @RolesAllowed("admin", "operator")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    suspend fun createApplication(
        @RestForm @NotBlank name: String?,
        @RestForm @NotBlank category: String?,
        @RestForm @NotBlank description: String?,
        @RestForm @NotBlank url: String?,
        @RestForm requiresVpn: Boolean,
        @RestForm managedBy: String?,
        @RestForm externalId: String?,
        @RestForm("logo") logo: FileUpload?,
    ): Response {
        logoValidationError(logo)?.let { return it }

        val upload = readLogo(logo)

        val entity = ApplicationEntity()
        entity.id = UUID.randomUUID()
        entity.name = requireNotNull(name).trim()
        entity.category = requireNotNull(category).trim()
        entity.description = requireNotNull(description).trim()
        entity.url = requireNotNull(url).trim()
        entity.requiresVpn = requiresVpn
        entity.managedBy = managedBy?.trim()?.takeIf { it.isNotEmpty() }
        entity.externalId = externalId?.trim()?.takeIf { it.isNotEmpty() }
        entity.logo = upload?.bytes
        entity.logoContentType = upload?.contentType
        entity.createdAt = LocalDateTime.now()

        val saved = applicationRepository.save(entity)

        return Response.status(Response.Status.CREATED).entity(saved.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin", "operator")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    suspend fun updateApplication(
        @PathParam("id") id: UUID,
        @RestForm @NotBlank name: String?,
        @RestForm @NotBlank category: String?,
        @RestForm @NotBlank description: String?,
        @RestForm @NotBlank url: String?,
        @RestForm requiresVpn: Boolean,
        @RestForm managedBy: String?,
        @RestForm externalId: String?,
        @RestForm("logo") logo: FileUpload?,
    ): Response {
        logoValidationError(logo)?.let { return it }

        val upload = readLogo(logo)

        val updated = applicationRepository.update(id) { entity ->
            entity.name = requireNotNull(name).trim()
            entity.category = requireNotNull(category).trim()
            entity.description = requireNotNull(description).trim()
            entity.url = requireNotNull(url).trim()
            entity.requiresVpn = requiresVpn
            // Champs absents du formulaire (admin UI) : on conserve les valeurs existantes
            managedBy?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.managedBy = it }
            externalId?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.externalId = it }
            if (upload != null) {
                entity.logo = upload.bytes
                entity.logoContentType = upload.contentType
            }
            entity.updatedAt = LocalDateTime.now()
        } ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(updated.toDto()).build()
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin", "operator")
    suspend fun deleteApplication(@PathParam("id") id: UUID): Response =
        if (applicationRepository.delete(id)) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }

    @GET
    @Path("/{id}/logo")
    @Produces(MediaType.WILDCARD)
    suspend fun getLogo(@PathParam("id") id: UUID): Response {
        val logo = applicationRepository.findLogo(id)
            ?: return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(logo.content)
            .type(logo.contentType)
            .header("Cache-Control", "public, max-age=86400")
            .build()
    }

    private fun logoValidationError(logo: FileUpload?): Response? {
        if (logo == null) return null
        if (logo.contentType() !in ALLOWED_LOGO_CONTENT_TYPES) {
            return badRequest("logo content type must be one of $ALLOWED_LOGO_CONTENT_TYPES")
        }
        if (logo.size() > MAX_LOGO_SIZE_BYTES) {
            return badRequest("logo must not exceed 1 MB")
        }
        return null
    }

    private data class LogoUpload(val bytes: ByteArray, val contentType: String?)

    // The upload lives in a temp file: read it with Vert.x before opening the reactive session
    private suspend fun readLogo(logo: FileUpload?): LogoUpload? = logo?.let {
        val bytes = vertx.fileSystem().readFile(it.uploadedFile().toString()).awaitSuspending().bytes
        LogoUpload(bytes, it.contentType())
    }

    private fun ApplicationEntity.toDto() =
        ApplicationDto(requireNotNull(id), name, category, description, url, requiresVpn, logo != null, managedBy, externalId, updatedAt)
}
