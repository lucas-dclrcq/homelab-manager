package org.hoohoot.homelab.manager.cleanup.domain.ports

interface DiskSpaceGauge {
    // Espace libre d'après le dernier snapshot stats (rafraîchi toutes les 15 min)
    suspend fun snapshotFree(path: String): Long?

    // Espace libre en direct (GET /diskspace Radarr) — re-check juste avant d'exécuter les suppressions
    suspend fun liveFree(path: String): Long?

    suspend fun knownPaths(): List<String>
}
