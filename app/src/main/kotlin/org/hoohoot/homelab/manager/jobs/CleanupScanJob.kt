package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.domain.CampaignTrigger
import org.hoohoot.homelab.manager.cleanup.domain.ScanResult
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ScanAndStartCampaign

@ApplicationScoped
class CleanupScanJob(
    private val scanAndStartCampaign: ScanAndStartCampaign,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "cleanup.scan.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Nettoyage : scan du seuil d'espace disque"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        when (val result = scanAndStartCampaign(CampaignTrigger.AUTO)) {
            is ScanResult.Started ->
                Log.info("Cleanup: campaign ${result.campaign.id} started with ${result.candidateCount} candidates")
            is ScanResult.ThresholdNotReached -> Log.debug("Cleanup: disk space above threshold, no campaign")
            ScanResult.AlreadyActive -> Log.debug("Cleanup: a campaign is already active")
            ScanResult.NoCandidates -> Log.info("Cleanup: below threshold but no eligible candidate")
            ScanResult.DiskSpaceUnknown -> Log.warn("Cleanup: disk space unknown for the configured path")
        }
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{cleanup.scan.every}",
        delayed = "{cleanup.scan.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running cleanup scan")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "cleanup-scan"
    }
}
