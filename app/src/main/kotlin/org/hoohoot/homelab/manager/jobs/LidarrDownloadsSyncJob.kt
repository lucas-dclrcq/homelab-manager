package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.library.infra.DownloadsSyncService

@ApplicationScoped
class LidarrDownloadsSyncJob(
    private val downloadsSyncService: DownloadsSyncService,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "downloads-sync.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Synchronisation des téléchargements Lidarr"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        downloadsSyncService.syncLidarr()
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{downloads-sync.every}",
        delayed = "{downloads-sync.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running Lidarr downloads sync")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "lidarr-downloads-sync"
    }
}
