package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.ports.CleanupConfigStore
import org.hoohoot.homelab.manager.cleanup.domain.ports.DiskSpaceGauge

data class EffectiveCleanupConfig(
    val config: CleanupConfig,
    val knownDiskPaths: List<String>,
    val diskFreeBytes: Long?,
)

@ApplicationScoped
class GetCleanupConfig(
    private val configStore: CleanupConfigStore,
    private val diskSpaceGauge: DiskSpaceGauge,
) {
    suspend operator fun invoke(): EffectiveCleanupConfig {
        val config = configStore.effective()
        return EffectiveCleanupConfig(
            config = config,
            knownDiskPaths = diskSpaceGauge.knownPaths(),
            diskFreeBytes = diskSpaceGauge.snapshotFree(config.diskPath),
        )
    }
}
