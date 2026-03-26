package org.hoohoot.homelab.manager.notifications.weeklyreport

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext

@ApplicationScoped
class WeeklyReportJob(private val weeklyReportService: WeeklyReportService) {
    @Scheduled(cron = "{weekly-report.cron}")
    @ActivateRequestContext
    suspend fun sendWeeklyReport() {
        Log.info("Scheduled weekly report job triggered")
        weeklyReportService.sendWeeklyReport()
    }
}
