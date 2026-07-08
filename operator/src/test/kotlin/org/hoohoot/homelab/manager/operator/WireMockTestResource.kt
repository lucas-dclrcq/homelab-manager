package org.hoohoot.homelab.manager.operator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

class WireMockTestResource : QuarkusTestResourceLifecycleManager {

    companion object {
        lateinit var server: WireMockServer

        fun stubTokenEndpoint() {
            server.stubFor(
                post(urlEqualTo("/token")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"access_token":"test-token","token_type":"Bearer","expires_in":3600}""")
                )
            )
        }
    }

    override fun start(): Map<String, String> {
        server = WireMockServer(options().dynamicPort())
        server.start()
        stubTokenEndpoint()

        return mapOf(
            "quarkus.rest-client.manager-api.url" to server.baseUrl(),
            "quarkus.oidc-client.auth-server-url" to server.baseUrl(),
            "quarkus.oidc-client.discovery-enabled" to "false",
            "quarkus.oidc-client.token-path" to "/token",
            "quarkus.oidc-client.early-tokens-acquisition" to "false",
        )
    }

    override fun stop() {
        server.stop()
    }
}
