package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.portal.stats.StatsSyncService

@ApplicationScoped
class SonarrSyncJob(
    private val statsSyncService: StatsSyncService,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "sonarr-sync.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Synchronisation des stats Sonarr"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        statsSyncService.syncSonarr()
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{sonarr-sync.every}",
        delayed = "{stats-sync.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running Sonarr stats sync")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "sonarr-sync"
    }
}
