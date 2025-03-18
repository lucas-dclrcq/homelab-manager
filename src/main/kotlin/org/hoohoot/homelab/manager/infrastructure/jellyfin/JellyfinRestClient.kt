package org.hoohoot.homelab.manager.infrastructure.jellyfin

import io.quarkus.rest.client.reactive.ClientQueryParam
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "jellyfin-api")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ClientQueryParam(name = "ApiKey", value = ["\${jellyfin.api_key}"])
interface JellyfinRestClient {
    @GET
    @Path("/Search/Hints")
    suspend fun getSearchHintResult(
        @QueryParam("searchTerm") searchTerm: String,
        @QueryParam("includeItemTypes") includeItemTypes: String?
    ): JellyfinSearchHintsResponse
}