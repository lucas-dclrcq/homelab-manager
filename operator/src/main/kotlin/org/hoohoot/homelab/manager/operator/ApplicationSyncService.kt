package org.hoohoot.homelab.manager.operator

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.rest.client.inject.RestClient

@ApplicationScoped
class ApplicationSyncService(@param:RestClient private val api: ManagerApiClient) {

    /** Réconciliation complète : upsert de toutes les routes désirées, suppression des apps managées orphelines */
    fun syncAll(desired: List<DesiredApplication>) {
        val managed = listManaged()
        val desiredByExternalId = desired.associateBy { it.externalId }

        desired.forEach { upsert(it, managed[it.externalId]) }

        managed.values
            .filter { it.externalId !in desiredByExternalId }
            .forEach { orphan ->
                Log.info("Deleting orphaned managed application '${orphan.name}' (${orphan.externalId})")
                api.delete(orphan.id).logIfFailed("delete ${orphan.externalId}")
            }
    }

    /** Upsert d'une seule route (chemin reconciler) */
    fun upsert(desired: DesiredApplication) {
        upsert(desired, listManaged()[desired.externalId])
    }

    /** Supprime l'app correspondant à la route si (et seulement si) elle est managée par l'opérateur */
    fun deleteIfManaged(externalId: String) {
        val existing = listManaged()[externalId] ?: return
        Log.info("Deleting managed application '${existing.name}' ($externalId)")
        api.delete(existing.id).logIfFailed("delete $externalId")
    }

    private fun listManaged(): Map<String, ApplicationDto> =
        api.list()
            .filter { it.managedBy == MANAGED_BY && it.externalId != null }
            .associateBy { it.externalId!! }

    private fun upsert(desired: DesiredApplication, current: ApplicationDto?) {
        when {
            current == null -> {
                Log.info("Creating application '${desired.name}' (${desired.externalId})")
                api.create(desired.toRequest()).logIfFailed("create ${desired.externalId}")
            }

            current.hasDriftedFrom(desired) -> {
                Log.info("Updating application '${desired.name}' (${desired.externalId})")
                api.update(current.id, desired.toRequest()).logIfFailed("update ${desired.externalId}")
            }
        }
    }

    private fun DesiredApplication.toRequest() =
        ApplicationRequest(name, category, description, url, requiresVpn, MANAGED_BY, externalId, logoUrl)

    // logoSourceUrl : un logo uploadé à la main a une source nulle, donc pas de drift tant que
    // le CRD ne déclare pas de logo-url ; un téléchargement échoué garde l'ancienne source et
    // sera retenté au prochain sweep
    private fun ApplicationDto.hasDriftedFrom(desired: DesiredApplication): Boolean =
        name != desired.name ||
            category != desired.category ||
            description != desired.description ||
            url != desired.url ||
            requiresVpn != desired.requiresVpn ||
            logoSourceUrl != desired.logoUrl

    private fun Response.logIfFailed(operation: String) {
        if (status !in 200..299) {
            Log.error("Manager API call failed ($operation): HTTP $status")
        }
        close()
    }
}
