package org.hoohoot.homelab.manager.portal.resource

import io.quarkus.security.identity.SecurityIdentity
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag

data class MeDto(val username: String, val roles: Set<String>)

@Path("/api/me")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portal")
class MeResource(private val identity: SecurityIdentity) {

    @GET
    fun getCurrentUser(): MeDto = MeDto(identity.principal.name, identity.roles)
}
