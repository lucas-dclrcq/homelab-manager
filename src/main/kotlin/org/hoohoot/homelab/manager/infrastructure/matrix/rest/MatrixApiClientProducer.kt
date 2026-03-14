package org.hoohoot.homelab.manager.infrastructure.matrix.rest

import io.ktor.http.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClient
import net.folivo.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import net.folivo.trixnity.clientserverapi.client.MatrixAuthProvider
import net.folivo.trixnity.clientserverapi.client.classicInMemory

@ApplicationScoped
class MatrixApiClientProducer(private val config: MatrixConfiguration) {

    @Produces
    @ApplicationScoped
    fun matrixApiClient(): MatrixClientServerApiClient {
        return MatrixClientServerApiClientImpl(
            baseUrl = Url(config.baseUrl()),
            authProvider = MatrixAuthProvider.classicInMemory(config.accessToken()),
        )
    }
}
