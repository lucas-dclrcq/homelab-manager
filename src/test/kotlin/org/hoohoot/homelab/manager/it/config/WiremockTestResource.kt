package org.hoohoot.homelab.manager.it.config

import com.github.tomakehurst.wiremock.WireMockServer
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager.TestInjector.AnnotatedAndMatchesType

class WiremockTestResource : QuarkusTestResourceLifecycleManager {
    private var wireMockServer: WireMockServer? = null

    override fun start(): Map<String, String> {
        wireMockServer = WireMockServer(0)
        wireMockServer!!.start()

        return mapOf(
            "quarkus.rest-client.matrix-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.sonarr-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.radarr-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.jellystat-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.jellyfin-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.lidarr-api.url" to wireMockServer!!.baseUrl(),
            "quarkus.rest-client.giphy-api.url" to wireMockServer!!.baseUrl()
        )
    }

    override fun stop() {
        if (null != wireMockServer) {
            wireMockServer!!.stop()
        }
    }

    override fun inject(testInjector: TestInjector) {
        testInjector.injectIntoFields(
            wireMockServer,
            AnnotatedAndMatchesType(InjectWireMock::class.java, WireMockServer::class.java)
        )
    }
}