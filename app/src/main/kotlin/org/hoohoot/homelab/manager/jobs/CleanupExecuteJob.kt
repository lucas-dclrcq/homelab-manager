package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ExecuteDueCampaigns

@ApplicationScoped
class CleanupExecuteJob(
    private val executeDueCampaigns: ExecuteDueCampaigns,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "cleanup.execute.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Nettoyage : suppressions à échéance de grâce"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        val summaries = executeDueCampaigns()
        summaries.forEach {
            Log.info(
                "Cleanup: campaign executed — deleted=${it.deletedCount} protected=${it.protectedCount} " +
                    "skipped=${it.skippedCount} failed=${it.failedCount} freedBytes=${it.freedBytes}",
            )
        }
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{cleanup.execute.every}",
        delayed = "{cleanup.execute.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running cleanup execution")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "cleanup-execute"
    }
}
