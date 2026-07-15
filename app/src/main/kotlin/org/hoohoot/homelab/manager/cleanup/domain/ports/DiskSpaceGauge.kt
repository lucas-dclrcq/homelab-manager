package org.hoohoot.homelab.manager.cleanup.domain.ports

interface DiskSpaceGauge {
    suspend fun snapshotFree(path: String): Long?

    suspend fun liveFree(path: String): Long?

    suspend fun knownPaths(): List<String>
}
