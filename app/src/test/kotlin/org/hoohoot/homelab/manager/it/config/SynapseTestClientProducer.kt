package org.hoohoot.homelab.manager.it.config

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.inject.Singleton
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.http.HttpClient

@ApplicationScoped
class SynapseTestClientProducer {

    @Produces
    @Singleton
    fun synapseTestClient(
        @ConfigProperty(name = "matrix.base-url") synapseUrl: String,
        @ConfigProperty(name = "matrix.access-token") accessToken: String
    ): SynapseTestClient {
        return SynapseTestClient(synapseUrl, accessToken, HttpClient.newHttpClient(), ObjectMapper())
    }
}
