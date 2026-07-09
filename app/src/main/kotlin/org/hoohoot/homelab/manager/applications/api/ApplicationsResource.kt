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
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.applications.domain.ALLOWED_LOGO_CONTENT_TYPES
import org.hoohoot.homelab.manager.applications.domain.ApplicationInput
import org.hoohoot.homelab.manager.applications.domain.LogoChange
import org.hoohoot.homelab.manager.applications.domain.LogoUpload
import org.hoohoot.homelab.manager.applications.domain.MAX_LOGO_SIZE_BYTES
import org.hoohoot.homelab.manager.applications.domain.usecases.CreateApplication
import org.hoohoot.homelab.manager.applications.domain.usecases.DeleteApplication
import org.hoohoot.homelab.manager.applications.domain.usecases.ListApplications
import org.hoohoot.homelab.manager.applications.domain.usecases.UpdateApplication
import org.hoohoot.homelab.manager.applications.infra.ApplicationRepository
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.jboss.resteasy.reactive.RestForm
import org.jboss.resteasy.reactive.multipart.FileUpload
import java.util.UUID

@Path("/api/applications")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portal")
class ApplicationsResource(
    private val listApplicationsUseCase: ListApplications,
    private val createApplicationUseCase: CreateApplication,
    private val updateApplicationUseCase: UpdateApplication,
    private val deleteApplicationUseCase: DeleteApplication,
    private val applicationRepository: ApplicationRepository,
    private val vertx: Vertx,
) {

    @GET
    suspend fun listApplications(): List<ApplicationDto> = listApplicationsUseCase().map { it.toDto() }

    @POST
    @RolesAllowed("admin")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @APIResponseSchema(value = ApplicationDto::class, responseCode = "201")
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

        val input = ApplicationInput(
            name = requireNotNull(name),
            category = requireNotNull(category),
            description = requireNotNull(description),
            url = requireNotNull(url),
            requiresVpn = requiresVpn,
            managedBy = managedBy,
            externalId = externalId,
            logo = readLogo(logo),
        )

        val saved = createApplicationUseCase(input)

        return Response.status(Response.Status.CREATED).entity(saved.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @APIResponseSchema(value = ApplicationDto::class, responseCode = "200")
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

        val input = ApplicationInput(
            name = requireNotNull(name),
            category = requireNotNull(category),
            description = requireNotNull(description),
            url = requireNotNull(url),
            requiresVpn = requiresVpn,
            managedBy = managedBy,
            externalId = externalId,
            logo = readLogo(logo),
        )

        val updated = updateApplicationUseCase(id, input)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(updated.toDto()).build()
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    suspend fun deleteApplication(@PathParam("id") id: UUID): Response =
        if (deleteApplicationUseCase(id)) {
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
    private suspend fun readLogo(logo: FileUpload?): LogoChange = logo?.let {
        val bytes = vertx.fileSystem().readFile(it.uploadedFile().toString()).awaitSuspending().bytes
        LogoChange.Upload(LogoUpload(bytes, it.contentType()))
    } ?: LogoChange.Keep
}
