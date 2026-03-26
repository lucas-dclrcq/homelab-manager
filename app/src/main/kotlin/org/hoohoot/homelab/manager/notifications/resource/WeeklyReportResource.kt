package org.hoohoot.homelab.manager.notifications.resource

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.weeklyreport.WeeklyReportService

@Path("/api/notifications/send-weekly-report")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class WeeklyReportResource(
    private val weeklyReportService: WeeklyReportService
) {

    @POST
    suspend fun sendWeeklyReport(): Response {
        Log.info("Manual weekly report trigger received")
        weeklyReportService.sendWeeklyReport()
        return Response.noContent().build()
    }
}
