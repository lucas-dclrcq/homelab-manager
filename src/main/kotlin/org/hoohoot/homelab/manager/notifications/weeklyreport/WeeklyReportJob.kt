package org.hoohoot.homelab.manager.notifications.weeklyreport

import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class WeeklyReportJob(private val weeklyReportService: WeeklyReportService) {
    @Scheduled(cron = "{weekly-report.cron}")
    suspend fun sendWeeklyReport() {
        weeklyReportService.sendWeeklyReport()
    }
}
