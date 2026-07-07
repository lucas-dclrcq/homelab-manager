package org.hoohoot.homelab.manager.portal.resource

import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.mutiny.core.Vertx
import jakarta.annotation.security.RolesAllowed
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
import org.hoohoot.homelab.manager.portal.persistence.ApplicationEntity
import org.hoohoot.homelab.manager.portal.persistence.ApplicationRepository
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
            ApplicationDto(it.id, it.name, it.category, it.description, it.url, it.requiresVpn, it.hasLogo, it.updatedAt)
        }

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    suspend fun createApplication(
        @RestForm name: String?,
        @RestForm category: String?,
        @RestForm description: String?,
        @RestForm url: String?,
        @RestForm requiresVpn: Boolean,
        @RestForm("logo") logo: FileUpload?,
    ): Response {
        if (name.isNullOrBlank() || category.isNullOrBlank() || description.isNullOrBlank() || url.isNullOrBlank()) {
            return badRequest("name, category, description and url are required")
        }
        logoValidationError(logo)?.let { return it }

        val logoBytes = readLogoBytes(logo)

        val entity = ApplicationEntity()
        entity.id = UUID.randomUUID()
        entity.name = name.trim()
        entity.category = category.trim()
        entity.description = description.trim()
        entity.url = url.trim()
        entity.requiresVpn = requiresVpn
        entity.logo = logoBytes
        entity.logoContentType = if (logoBytes != null) logo?.contentType() else null
        entity.createdAt = LocalDateTime.now()

        val saved = applicationRepository.save(entity)

        return Response.status(Response.Status.CREATED).entity(saved.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    suspend fun updateApplication(
        @PathParam("id") id: UUID,
        @RestForm name: String?,
        @RestForm category: String?,
        @RestForm description: String?,
        @RestForm url: String?,
        @RestForm requiresVpn: Boolean,
        @RestForm("logo") logo: FileUpload?,
    ): Response {
        if (name.isNullOrBlank() || category.isNullOrBlank() || description.isNullOrBlank() || url.isNullOrBlank()) {
            return badRequest("name, category, description and url are required")
        }
        logoValidationError(logo)?.let { return it }

        val logoBytes = readLogoBytes(logo)

        val updated = applicationRepository.update(id) { entity ->
            entity.name = name.trim()
            entity.category = category.trim()
            entity.description = description.trim()
            entity.url = url.trim()
            entity.requiresVpn = requiresVpn
            if (logoBytes != null) {
                entity.logo = logoBytes
                entity.logoContentType = logo!!.contentType()
            }
            entity.updatedAt = LocalDateTime.now()
        } ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(updated.toDto()).build()
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
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

    // The upload lives in a temp file: read it with Vert.x before opening the reactive session
    private suspend fun readLogoBytes(logo: FileUpload?): ByteArray? = logo?.let {
        vertx.fileSystem().readFile(it.uploadedFile().toString()).awaitSuspending().bytes
    }

    private fun ApplicationEntity.toDto() =
        ApplicationDto(id!!, name, category, description, url, requiresVpn, logo != null, updatedAt)

    private fun badRequest(message: String) =
        Response.status(Response.Status.BAD_REQUEST).entity(mapOf("error" to message)).build()
}
