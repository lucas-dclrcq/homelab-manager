package org.hoohoot.homelab.manager.notifications.matrix

import io.ktor.http.*
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClient
import de.connect2x.trixnity.clientserverapi.client.MatrixClientServerApiClientImpl
import de.connect2x.trixnity.clientserverapi.client.ClassicMatrixClientAuthProvider
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderData
import de.connect2x.trixnity.clientserverapi.client.MatrixClientAuthProviderDataStore
import de.connect2x.trixnity.clientserverapi.client.classic

@ApplicationScoped
class MatrixApiClientProducer(private val config: MatrixConfiguration) {

    @Produces
    @ApplicationScoped
    fun matrixApiClient(): MatrixClientServerApiClient {
        return MatrixClientServerApiClientImpl(
            authProvider = ClassicMatrixClientAuthProvider(
                baseUrl = Url(config.baseUrl()),
                store = MatrixClientAuthProviderDataStore.inMemory(
                    MatrixClientAuthProviderData.classic(
                        baseUrl = Url(config.baseUrl()),
                        accessToken = config.accessToken(),
                    )
                ),
                onLogout = {},
            ),
        )
    }
}
