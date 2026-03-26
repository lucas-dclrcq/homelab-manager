package org.hoohoot.homelab.manager.notifications.weeklyreport

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.hoohoot.homelab.manager.leader.NotLeader

@ApplicationScoped
class WeeklyReportJob(private val weeklyReportService: WeeklyReportService) {
    @Scheduled(cron = "{weekly-report.cron}", skipExecutionIf = NotLeader::class)
    @ActivateRequestContext
    suspend fun sendWeeklyReport() {
        Log.info("Scheduled weekly report job triggered")
        weeklyReportService.sendWeeklyReport()
    }
}
