package org.hoohoot.homelab.manager.notifications.infrastructure.api

import io.quarkus.security.UnauthorizedException
import jakarta.ws.rs.container.ContainerRequestContext
import org.jboss.resteasy.reactive.server.ServerRequestFilter

private const val API_KEY_HEADER = "X-Api-Key"

class ApiKeyAuthFilter(private val apiAuthConfiguration: ApiAuthConfiguration)  {
    @ServerRequestFilter(preMatching = true)
    fun filterApiKey(containerRequestContext: ContainerRequestContext) {
        if (!apiAuthConfiguration.enabled()) return
        if (apiAuthConfiguration.endpoints() != null && !apiAuthConfiguration.endpoints()!!.toRegex().matches(containerRequestContext.uriInfo.path)) return

        val apiKeyHeader = containerRequestContext.getHeaderString(API_KEY_HEADER)
        if (apiKeyHeader == null || apiKeyHeader != apiAuthConfiguration.apiKey()) {
            throw UnauthorizedException("API key is missing or invalid")
        }
    }
}