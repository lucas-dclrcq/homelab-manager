package org.hoohoot.homelab.manager.notifications.weeklyreport

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class WeeklyReportJob(private val weeklyReportService: WeeklyReportService) {
    @Scheduled(cron = "{weekly-report.cron}")
    suspend fun sendWeeklyReport() {
        Log.info("Scheduled weekly report job triggered")
        weeklyReportService.sendWeeklyReport()
    }
}
