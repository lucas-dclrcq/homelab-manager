package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.finances.domain.usecases.CreateMonthlyEnergyExpense

@ApplicationScoped
class EnergyExpenseJob(
    private val createMonthlyEnergyExpense: CreateMonthlyEnergyExpense,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "energy-expense.cron") private val cron: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Dépense énergétique mensuelle"
    override val schedule get() = "cron $cron"

    override suspend fun execute() {
        createMonthlyEnergyExpense()
    }

    @Scheduled(
        identity = IDENTITY,
        cron = "{energy-expense.cron}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun run() {
        Log.info("Running monthly energy expense creation")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "energy-expense"
    }
}
