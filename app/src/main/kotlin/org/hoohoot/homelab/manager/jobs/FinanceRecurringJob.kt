package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.finances.domain.usecases.GenerateRecurringEntries

@ApplicationScoped
class FinanceRecurringJob(
    private val generateRecurringEntries: GenerateRecurringEntries,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "finance-recurring.cron") private val cron: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Génération des écritures récurrentes"
    override val schedule get() = "cron $cron"

    override suspend fun execute() {
        generateRecurringEntries()
    }

    @Scheduled(
        identity = IDENTITY,
        cron = "{finance-recurring.cron}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running recurring finance entries generation")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "finance-recurring"
    }
}
