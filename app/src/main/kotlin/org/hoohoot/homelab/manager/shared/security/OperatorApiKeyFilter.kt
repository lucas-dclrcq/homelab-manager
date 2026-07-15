package org.hoohoot.homelab.manager.shared.security

import jakarta.ws.rs.NameBinding
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.MessageDigest
import java.util.Optional

private const val API_KEY_HEADER = "X-Api-Key"

@NameBinding
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class OperatorApiKeyProtected

@Provider
@OperatorApiKeyProtected
class OperatorApiKeyFilter(
    @param:ConfigProperty(name = "operator.api-key") private val configuredKey: Optional<String>,
) : ContainerRequestFilter {

    override fun filter(containerRequestContext: ContainerRequestContext) {
        // abortWith plutôt qu'UnauthorizedException : l'exception déclencherait le challenge OIDC (302 vers le login)
        val expected = configuredKey.orElse("").takeIf { it.isNotBlank() }
            ?: return containerRequestContext.abortWithUnauthorized()
        val provided = containerRequestContext.getHeaderString(API_KEY_HEADER)
            ?: return containerRequestContext.abortWithUnauthorized()
        if (!MessageDigest.isEqual(provided.toByteArray(), expected.toByteArray())) {
            containerRequestContext.abortWithUnauthorized()
        }
    }

    private fun ContainerRequestContext.abortWithUnauthorized() =
        abortWith(Response.status(Response.Status.UNAUTHORIZED).build())
}
