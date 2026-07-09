package org.hoohoot.homelab.manager.operator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApplicationDto(
    val id: UUID,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val managedBy: String? = null,
    val externalId: String? = null,
)

data class ApplicationRequest(
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val managedBy: String,
    val externalId: String,
)

// Auth par clé partagée : header X-Api-Key statique via quarkus.rest-client.manager-api.headers
@Path("/api/operator/applications")
@RegisterRestClient(configKey = "manager-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
interface ManagerApiClient {

    @GET
    fun list(): List<ApplicationDto>

    @POST
    fun create(request: ApplicationRequest): Response

    @PUT
    @Path("/{id}")
    fun update(@PathParam("id") id: UUID, request: ApplicationRequest): Response

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: UUID): Response
}
