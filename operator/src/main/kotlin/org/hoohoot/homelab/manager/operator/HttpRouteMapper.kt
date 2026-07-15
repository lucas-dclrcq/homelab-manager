package org.hoohoot.homelab.manager.operator

import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRoute
import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped

data class DesiredApplication(
    val externalId: String,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val logoUrl: String? = null,
)

const val MANAGED_BY = "operator"
const val DEFAULT_DESCRIPTION = "Managed by homelab-manager-operator"

@ApplicationScoped
class HttpRouteMapper(private val config: OperatorConfig) {

    fun map(route: HTTPRoute): DesiredApplication? {
        val annotations = route.metadata?.annotations.orEmpty()
        val prefix = config.annotationPrefix()

        if (annotations["$prefix/enabled"] != "true") return null

        val externalId = "${route.metadata.namespace}/${route.metadata.name}"
        val url = annotations["$prefix/url"]?.takeIf { it.isNotBlank() }
            ?: route.spec?.hostnames?.firstOrNull()?.let { "https://$it" }
        if (url == null) {
            Log.warn("HTTPRoute $externalId is enabled but has no hostname and no url annotation, skipping")
            return null
        }

        val requiresVpn = route.spec?.parentRefs.orEmpty().any { it.name in config.vpnGateways() }

        return DesiredApplication(
            externalId = externalId,
            name = annotations["$prefix/name"]?.takeIf { it.isNotBlank() } ?: route.metadata.name,
            category = annotations["$prefix/category"]?.takeIf { it.isNotBlank() } ?: config.defaultCategory(),
            description = annotations["$prefix/description"]?.takeIf { it.isNotBlank() } ?: DEFAULT_DESCRIPTION,
            url = url,
            requiresVpn = requiresVpn,
            logoUrl = annotations["$prefix/logo-url"]?.takeIf { it.isNotBlank() },
        )
    }
}
