package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.logging.Log
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.cleanup.domain.ports.DiskSpaceGauge
import org.hoohoot.homelab.manager.library.infra.StatsSnapshotEntity
import org.hoohoot.homelab.manager.shared.arr.radarr.RadarrRestClient

@ApplicationScoped
class ArrDiskSpaceAdapter(
    @param:RestClient private val radarrRestClient: RadarrRestClient,
) : DiskSpaceGauge {

    override suspend fun snapshotFree(path: String): Long? =
        snapshots()
            .sortedByDescending { it.collectedAt }
            .firstNotNullOfOrNull { it.disks[path]?.freeBytes }

    // Best-effort : en cas d'échec on retombera sur le snapshot
    override suspend fun liveFree(path: String): Long? = try {
        radarrRestClient.getDiskSpace().orEmpty().firstOrNull { it.path == path }?.freeSpace
    } catch (exception: Exception) {
        Log.warn("Cleanup: live disk space check failed for '$path'", exception)
        null
    }

    override suspend fun knownPaths(): List<String> =
        snapshots().flatMap { it.disks.keys }.distinct().sorted()

    private suspend fun snapshots(): List<StatsSnapshotEntity> =
        Panache.withSession { StatsSnapshotEntity.listAll() }.awaitSuspending()
}
