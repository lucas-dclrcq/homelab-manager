package org.hoohoot.homelab.manager.shared.authentik

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.ProcessingException
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import java.time.temporal.ChronoUnit

data class AuthentikPagination(
    // Authentik renvoie 0 (et non null) quand il n'y a pas de page suivante
    @field:JsonProperty("next")
    val next: Int? = null,

    @field:JsonProperty("total_pages")
    val totalPages: Int? = null,

    @field:JsonProperty("count")
    val count: Int? = null,
)

data class AuthentikUser(
    @field:JsonProperty("pk")
    val pk: Int,

    @field:JsonProperty("username")
    val username: String,

    @field:JsonProperty("name")
    val name: String? = null,

    @field:JsonProperty("email")
    val email: String? = null,

    @field:JsonProperty("is_active")
    val isActive: Boolean = true,
)

data class AuthentikUsersPage(
    @field:JsonProperty("pagination")
    val pagination: AuthentikPagination = AuthentikPagination(),

    @field:JsonProperty("results")
    val results: List<AuthentikUser> = emptyList(),
)

@Path("/api/v3")
@RegisterRestClient(configKey = "authentik-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "Authorization", value = ["Bearer \${authentik.token}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
interface AuthentikRestClient {
    @GET
    @Path("/core/users/")
    suspend fun getUsers(
        @QueryParam("page") page: Int,
        @QueryParam("page_size") pageSize: Int,
        @QueryParam("groups_by_name") group: String?,
    ): AuthentikUsersPage
}
