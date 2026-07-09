package org.hoohoot.homelab.manager.applications.api

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
import org.hoohoot.homelab.manager.applications.domain.ApplicationInput
import org.hoohoot.homelab.manager.applications.domain.usecases.CreateApplication
import org.hoohoot.homelab.manager.applications.domain.usecases.DeleteApplication
import org.hoohoot.homelab.manager.applications.domain.usecases.ListApplications
import org.hoohoot.homelab.manager.applications.domain.usecases.UpdateApplication
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.security.OperatorApiKeyProtected
import java.util.UUID

data class OperatorApplicationRequest(
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val managedBy: String? = null,
    val externalId: String? = null,
)

/**
 * Duplication des endpoints applications pour l'opérateur k8s : auth par clé partagée
 * (voir OperatorApiKeyFilter) au lieu d'OIDC, JSON au lieu de multipart (pas de logo).
 * Exclu du contrat OpenAPI consommé par Orval (mp.openapi.scan.exclude.classes).
 */
@Path("/api/operator/applications")
@OperatorApiKeyProtected
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class OperatorApplicationsResource(
    private val listApplications: ListApplications,
    private val createApplication: CreateApplication,
    private val updateApplication: UpdateApplication,
    private val deleteApplication: DeleteApplication,
) {

    @GET
    suspend fun list(): List<ApplicationDto> = listApplications().map { it.toDto() }

    @POST
    suspend fun create(request: OperatorApplicationRequest): Response {
        validationError(request)?.let { return it }

        val saved = createApplication(request.toInput())

        return Response.status(Response.Status.CREATED).entity(saved.toDto()).build()
    }

    @PUT
    @Path("/{id}")
    suspend fun update(@PathParam("id") id: UUID, request: OperatorApplicationRequest): Response {
        validationError(request)?.let { return it }

        val updated = updateApplication(id, request.toInput())
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        return Response.ok(updated.toDto()).build()
    }

    @DELETE
    @Path("/{id}")
    suspend fun delete(@PathParam("id") id: UUID): Response =
        if (deleteApplication(id)) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }

    private fun OperatorApplicationRequest.toInput() =
        ApplicationInput(name, category, description, url, requiresVpn, managedBy, externalId)

    private fun validationError(request: OperatorApplicationRequest): Response? =
        listOf(
            "name" to request.name,
            "category" to request.category,
            "description" to request.description,
            "url" to request.url,
        ).firstOrNull { it.second.isBlank() }?.let { badRequest("${it.first} must not be blank") }
}
