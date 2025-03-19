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

        return java.util.Map.of(
            "quarkus.rest-client.matrix-api.url", wireMockServer!!.baseUrl(),
            "quarkus.rest-client.sonarr-api.url", wireMockServer!!.baseUrl(),
            "quarkus.rest-client.jellystat-api.url", wireMockServer!!.baseUrl(),
            "quarkus.rest-client.jellyfin-api.url", wireMockServer!!.baseUrl()
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