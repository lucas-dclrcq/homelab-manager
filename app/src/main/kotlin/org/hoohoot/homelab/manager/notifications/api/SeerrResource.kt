package org.hoohoot.homelab.manager.notifications.api

import io.quarkus.logging.Log
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.notifications.domain.usecases.IssueReport
import org.hoohoot.homelab.manager.notifications.domain.usecases.NotifyIssueEvent

@Path("/api/notifications/seerr")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Notifications")
class SeerrResource(
    private val notifyIssueEvent: NotifyIssueEvent,
) {

    @POST
    suspend fun handleSeerrNotification(payload: SeerrWebhookPayload): Response {
        val issue = payload.toIssueReport()
        when (payload.notificationType()) {
            "ISSUE_CREATED" -> {
                Log.info("Notifying issue created : ${payload.title()}")
                notifyIssueEvent.created(issue)
            }
            "ISSUE_RESOLVED" -> {
                Log.info("Notifying issue resolved : ${payload.title()}")
                notifyIssueEvent.resolved(issue)
            }
            "ISSUE_REOPENED" -> {
                Log.info("Notifying issue reopened : ${payload.title()}")
                notifyIssueEvent.reopened(issue)
            }
            "ISSUE_COMMENT" -> {
                Log.info("Notifying issue commented : ${payload.title()}")
                notifyIssueEvent.commented(issue)
            }
            else -> Log.warn("Unhandled seerr type: ${payload.notificationType()}")
        }

        return Response.noContent().build()
    }

    private fun SeerrWebhookPayload.toIssueReport() = IssueReport(
        issueId = issueId(),
        title = title(),
        subject = subject(),
        message = message(),
        reporter = reportedByUserName(),
        additionalInfo = additionalInfo(),
        commentMessage = commentMessage(),
        commentedBy = commentedBy(),
    )
}
