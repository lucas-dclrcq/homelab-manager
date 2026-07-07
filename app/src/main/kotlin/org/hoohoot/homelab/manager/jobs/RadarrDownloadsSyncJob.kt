package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.portal.downloads.DownloadsSyncService

@ApplicationScoped
class RadarrDownloadsSyncJob(
    private val downloadsSyncService: DownloadsSyncService,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "downloads-sync.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Synchronisation des téléchargements Radarr"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        downloadsSyncService.syncRadarr()
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{downloads-sync.every}",
        delayed = "{downloads-sync.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running Radarr downloads sync")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "radarr-downloads-sync"
    }
}
