package org.hoohoot.homelab.manager.notifications

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.hoohoot.homelab.manager.leader.NotLeader
import org.hoohoot.homelab.manager.notifications.persistence.NotificationSentRepository

@ApplicationScoped
class NotificationCleanupJob(private val repo: NotificationSentRepository) {

    @Scheduled(every = "15m", skipExecutionIf = NotLeader::class)
    @ActivateRequestContext
    suspend fun cleanupExpiredThreads() {
        Log.info("Running cleanup of expired media notification threads")
        repo.deleteExpiredThreads()
    }
}
