package org.hoohoot.homelab.manager.operator

import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRoute
import io.fabric8.kubernetes.client.KubernetesClient
import io.quarkus.logging.Log
import io.quarkus.runtime.StartupEvent
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes

@ApplicationScoped
class FullSyncJob(
    private val client: KubernetesClient,
    private val mapper: HttpRouteMapper,
    private val syncService: ApplicationSyncService,
    private val config: OperatorConfig,
) {

    fun onStart(@Observes event: StartupEvent) {
        if (config.syncOnStart()) {
            runCatching { sync() }
                .onFailure { Log.error("Initial full sync failed, next attempt at the scheduled interval", it) }
        }
    }

    @Scheduled(every = "{homelab-operator.sync-interval}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun sync() {
        val routes = client.resources(HTTPRoute::class.java).inAnyNamespace().list().items
        val desired = routes.mapNotNull(mapper::map)
        Log.info("Full sync: ${routes.size} HTTPRoutes in cluster, ${desired.size} enabled")
        syncService.syncAll(desired)
    }
}
