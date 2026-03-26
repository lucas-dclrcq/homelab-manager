package org.hoohoot.homelab.manager.it.config

import com.github.tomakehurst.wiremock.client.WireMock
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class WireMockServerProducer {

    @Produces
    @Singleton
    fun wireMock(
        @ConfigProperty(name = "quarkus.wiremock.devservices.port") port: Int
    ): WireMock {
        return WireMock(port)
    }
}
