package org.hoohoot.homelab.manager.shared.matrix.bot

import de.connect2x.trixnity.clientserverapi.client.SyncState
import io.smallrye.health.api.Wellness
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse

@Wellness
@ApplicationScoped
class MatrixBotHealthCheck(private val lifecycle: MatrixBotLifecycle) : HealthCheck {

    override fun call(): HealthCheckResponse {
        val status = lifecycle.status
        val syncState = lifecycle.currentSyncState()

        val up = when (status) {
            MatrixBotStatus.DISABLED -> true
            MatrixBotStatus.RUNNING -> syncState != SyncState.ERROR
            MatrixBotStatus.CONNECTING, MatrixBotStatus.STOPPED -> false
        }

        val builder = HealthCheckResponse.named("matrix-bot")
            .status(up)
            .withData("status", status.name.lowercase())
        syncState?.let { builder.withData("syncState", it.name) }
        return builder.build()
    }
}
