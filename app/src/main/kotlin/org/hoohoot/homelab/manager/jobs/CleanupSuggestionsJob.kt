package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ExecuteDueSuggestions

@ApplicationScoped
class CleanupSuggestionsJob(
    private val executeDueSuggestions: ExecuteDueSuggestions,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "cleanup.suggestion.every") private val every: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Nettoyage : suggestions de suppression à échéance"
    override val schedule get() = "every $every"

    override suspend fun execute() {
        val executed = executeDueSuggestions()
        executed.forEach {
            Log.info("Cleanup: suggestion executed — '${it.displayTitle()}' status=${it.status} freedBytes=${it.freedBytes ?: 0}")
        }
    }

    @Scheduled(
        identity = IDENTITY,
        every = "{cleanup.suggestion.every}",
        delayed = "{cleanup.suggestion.initial-delay}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running cleanup suggestions execution")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "cleanup-suggestions"
    }
}
