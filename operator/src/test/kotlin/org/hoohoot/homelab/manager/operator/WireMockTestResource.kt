package org.hoohoot.homelab.manager.operator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager

const val TEST_API_KEY = "test-key"

class WireMockTestResource : QuarkusTestResourceLifecycleManager {

    companion object {
        lateinit var server: WireMockServer
    }

    override fun start(): Map<String, String> {
        server = WireMockServer(options().dynamicPort())
        server.start()

        return mapOf(
            "quarkus.rest-client.manager-api.url" to server.baseUrl(),
            "quarkus.rest-client.manager-api.headers.X-Api-Key" to TEST_API_KEY,
        )
    }

    override fun stop() {
        server.stop()
    }
}
