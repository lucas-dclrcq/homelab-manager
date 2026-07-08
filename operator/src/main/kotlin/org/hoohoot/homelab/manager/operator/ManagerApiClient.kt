package org.hoohoot.homelab.manager.operator

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.quarkus.oidc.client.filter.OidcClientFilter
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
import org.jboss.resteasy.reactive.RestForm
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

@Path("/api/applications")
@RegisterRestClient(configKey = "manager-api")
@OidcClientFilter
interface ManagerApiClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun list(): List<ApplicationDto>

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun create(
        @RestForm name: String,
        @RestForm category: String,
        @RestForm description: String,
        @RestForm url: String,
        @RestForm requiresVpn: String,
        @RestForm managedBy: String,
        @RestForm externalId: String,
    ): Response

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    fun update(
        @PathParam("id") id: UUID,
        @RestForm name: String,
        @RestForm category: String,
        @RestForm description: String,
        @RestForm url: String,
        @RestForm requiresVpn: String,
        @RestForm managedBy: String,
        @RestForm externalId: String,
    ): Response

    @DELETE
    @Path("/{id}")
    fun delete(@PathParam("id") id: UUID): Response
}
