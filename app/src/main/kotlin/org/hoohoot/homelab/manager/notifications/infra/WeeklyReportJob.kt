package org.hoohoot.homelab.manager.notifications.infra

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.jobs.JobRunner
import org.hoohoot.homelab.manager.jobs.ManagedJob
import org.hoohoot.homelab.manager.notifications.domain.usecases.SendWeeklyReport

@ApplicationScoped
class WeeklyReportJob(
    private val sendWeeklyReport: SendWeeklyReport,
    private val jobRunner: JobRunner,
    @param:ConfigProperty(name = "weekly-report.cron") private val cron: String,
) : ManagedJob {
    override val identity = IDENTITY
    override val displayName = "Rapport hebdomadaire"
    override val schedule get() = "cron $cron"

    override suspend fun execute() {
        sendWeeklyReport()
    }

    @Scheduled(identity = IDENTITY, cron = "{weekly-report.cron}")
    @ActivateRequestContext
    suspend fun sendWeeklyReport() {
        Log.info("Scheduled weekly report job triggered")
        jobRunner.runScheduled(this)
    }

    companion object {
        const val IDENTITY = "weekly-report"
    }
}
