package org.hoohoot.homelab.manager.operator

import io.fabric8.kubernetes.api.model.gatewayapi.v1.HTTPRoute
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.api.reconciler.Reconciler
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl
import jakarta.enterprise.context.ApplicationScoped

/**
 * Pas de Cleaner/finalizer : les HTTPRoute appartiennent à d'autres outils (Flux, Helm),
 * un finalizer bloquerait leur suppression si l'opérateur est down. Les routes supprimées
 * sont rattrapées par le sweep périodique de [FullSyncJob].
 */
@ApplicationScoped
class HttpRouteReconciler(
    private val mapper: HttpRouteMapper,
    private val syncService: ApplicationSyncService,
) : Reconciler<HTTPRoute> {

    override fun reconcile(route: HTTPRoute, context: Context<HTTPRoute>): UpdateControl<HTTPRoute> {
        val desired = mapper.map(route)
        if (desired != null) {
            syncService.upsert(desired)
        } else {
            syncService.deleteIfManaged("${route.metadata.namespace}/${route.metadata.name}")
        }
        return UpdateControl.noUpdate()
    }
}
