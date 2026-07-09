package org.hoohoot.homelab.manager.operator

import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRoute
import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRouteBuilder
import io.fabric8.kubernetes.api.model.gatewayapi.v1.ParentReferenceBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class HttpRouteMapperTest {

    private val config = object : OperatorConfig {
        override fun vpnGateways() = setOf("internal")
        override fun annotationPrefix() = "homelab-manager.hoohoot.org"
        override fun defaultCategory() = "Uncategorized"
        override fun syncInterval() = "5m"
        override fun syncOnStart() = false
    }

    private val mapper = HttpRouteMapper(config)

    private fun route(
        name: String = "jellyfin",
        namespace: String = "media",
        annotations: Map<String, String> = mapOf("homelab-manager.hoohoot.org/enabled" to "true"),
        hostnames: List<String> = listOf("jellyfin.example.org"),
        parentRefs: List<String> = listOf("external"),
    ): HTTPRoute = HTTPRouteBuilder()
        .withNewMetadata()
        .withName(name)
        .withNamespace(namespace)
        .withAnnotations<String, String>(annotations)
        .endMetadata()
        .withNewSpec()
        .withHostnames(hostnames)
        .withParentRefs(parentRefs.map { ParentReferenceBuilder().withName(it).build() })
        .endSpec()
        .build()

    @Test
    fun `maps an enabled route with defaults`() {
        val desired = mapper.map(route())

        assertThat(desired).isEqualTo(
            DesiredApplication(
                externalId = "media/jellyfin",
                name = "jellyfin",
                category = "Uncategorized",
                description = DEFAULT_DESCRIPTION,
                url = "https://jellyfin.example.org",
                requiresVpn = false,
            )
        )
    }

    @Test
    fun `ignores a route without the enabled annotation`() {
        assertThat(mapper.map(route(annotations = emptyMap()))).isNull()
    }

    @Test
    fun `ignores a route with enabled set to false`() {
        assertThat(mapper.map(route(annotations = mapOf("homelab-manager.hoohoot.org/enabled" to "false")))).isNull()
    }

    @Test
    fun `uses name, category and description annotations when present`() {
        val desired = mapper.map(
            route(
                annotations = mapOf(
                    "homelab-manager.hoohoot.org/enabled" to "true",
                    "homelab-manager.hoohoot.org/name" to "Jellyfin",
                    "homelab-manager.hoohoot.org/category" to "Médias",
                    "homelab-manager.hoohoot.org/description" to "Streaming du homelab",
                )
            )
        )

        assertThat(desired?.name).isEqualTo("Jellyfin")
        assertThat(desired?.category).isEqualTo("Médias")
        assertThat(desired?.description).isEqualTo("Streaming du homelab")
    }

    @Test
    fun `maps the logo-url annotation when present`() {
        val desired = mapper.map(
            route(
                annotations = mapOf(
                    "homelab-manager.hoohoot.org/enabled" to "true",
                    "homelab-manager.hoohoot.org/logo-url" to "https://cdn.jsdelivr.net/gh/homarr-labs/dashboard-icons/svg/jellyfin.svg",
                )
            )
        )

        assertThat(desired?.logoUrl).isEqualTo("https://cdn.jsdelivr.net/gh/homarr-labs/dashboard-icons/svg/jellyfin.svg")
    }

    @Test
    fun `ignores a blank logo-url annotation`() {
        val desired = mapper.map(
            route(
                annotations = mapOf(
                    "homelab-manager.hoohoot.org/enabled" to "true",
                    "homelab-manager.hoohoot.org/logo-url" to "  ",
                )
            )
        )

        assertThat(desired?.logoUrl).isNull()
    }

    @Test
    fun `url annotation overrides the hostname`() {
        val desired = mapper.map(
            route(
                annotations = mapOf(
                    "homelab-manager.hoohoot.org/enabled" to "true",
                    "homelab-manager.hoohoot.org/url" to "https://media.example.org/jellyfin",
                )
            )
        )

        assertThat(desired?.url).isEqualTo("https://media.example.org/jellyfin")
    }

    @Test
    fun `derives the url from the first hostname`() {
        val desired = mapper.map(route(hostnames = listOf("first.example.org", "second.example.org")))

        assertThat(desired?.url).isEqualTo("https://first.example.org")
    }

    @Test
    fun `skips an enabled route without hostname nor url annotation`() {
        assertThat(mapper.map(route(hostnames = emptyList()))).isNull()
    }

    @Test
    fun `requires vpn when a parent ref targets a vpn gateway`() {
        assertThat(mapper.map(route(parentRefs = listOf("internal")))?.requiresVpn).isTrue()
    }

    @Test
    fun `requires vpn when any of several parent refs targets a vpn gateway`() {
        assertThat(mapper.map(route(parentRefs = listOf("external", "internal")))?.requiresVpn).isTrue()
    }

    @Test
    fun `does not require vpn when no parent ref targets a vpn gateway`() {
        assertThat(mapper.map(route(parentRefs = listOf("external")))?.requiresVpn).isFalse()
    }
}
