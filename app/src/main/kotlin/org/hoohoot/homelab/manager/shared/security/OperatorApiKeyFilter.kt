package org.hoohoot.homelab.manager.shared.security

import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.vertx.http.runtime.security.HttpSecurityPolicy
import io.smallrye.mutiny.Uni
import io.vertx.ext.web.RoutingContext
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.security.MessageDigest
import java.util.*

private const val API_KEY_HEADER = "X-Api-Key"

@ApplicationScoped
class OperatorApiKeyPolicy(
    @param:ConfigProperty(name = "operator.api-key") private val configuredKey: Optional<String>
) : HttpSecurityPolicy {
    override fun checkPermission(
        request: RoutingContext?,
        identity: Uni<SecurityIdentity?>?,
        requestContext: HttpSecurityPolicy.AuthorizationRequestContext?
    ): Uni<HttpSecurityPolicy.CheckResult?>? {
        val expected = configuredKey.orElse("").takeIf { it.isNotBlank() }
            ?: return HttpSecurityPolicy.CheckResult.deny();
        val provided = request?.request()?.getHeader(API_KEY_HEADER)
            ?: return HttpSecurityPolicy.CheckResult.deny();
        if (!MessageDigest.isEqual(provided.toByteArray(), expected.toByteArray())) {
            return HttpSecurityPolicy.CheckResult.deny();
        }
        return HttpSecurityPolicy.CheckResult.permit();
    }

    override fun name() = "operator"
}