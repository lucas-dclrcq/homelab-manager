package org.hoohoot.homelab.manager.notifications

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.hoohoot.homelab.manager.jobs.JobRunner
import org.hoohoot.homelab.manager.jobs.ManagedJob
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@ApplicationScoped
class NotificationCleanupJob(
    private val repo: NotificationSentRepository,
    private val jobRunner: JobRunner,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Nettoyage des notifications expirées"
    override val schedule = "every 15m"

    override suspend fun execute() {
        repo.deleteExpiredThreads()
    }

    @Scheduled(identity = IDENTITY, every = "15m")
    @ActivateRequestContext
    suspend fun cleanupExpiredThreads() {
        Log.info("Running cleanup of expired media notification threads")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "notification-cleanup"
    }
}
