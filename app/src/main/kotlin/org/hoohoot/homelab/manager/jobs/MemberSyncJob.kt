package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.members.domain.usecases.SyncMembers

@ApplicationScoped
class MemberSyncJob(
    private val syncMembers: SyncMembers,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "member-sync.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Synchronisation des membres Authentik"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        syncMembers()
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{member-sync.every}",
        delayed = "{member-sync.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running Authentik member sync")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "member-sync"
    }
}
