package org.hoohoot.homelab.manager.portal.resource

import io.quarkus.scheduler.Scheduler
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.jobs.JobRunner
import org.hoohoot.homelab.manager.portal.persistence.JobExecutionEntity
import org.hoohoot.homelab.manager.portal.persistence.JobExecutionRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class JobExecutionDto(
    val runAt: LocalDateTime,
    val status: String,
    val durationMs: Long?,
    val error: String?,
    val manual: Boolean,
)

data class JobStatusDto(
    val identity: String,
    val displayName: String?,
    val schedule: String?,
    val nextFireTime: LocalDateTime?,
    val paused: Boolean,
    val runnable: Boolean,
    val lastExecution: JobExecutionDto?,
)

@Path("/api/admin/jobs")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name = "Portal")
class JobsResource(
    private val scheduler: Scheduler,
    private val jobRunner: JobRunner,
    private val jobExecutionRepository: JobExecutionRepository,
) {

    @GET
    suspend fun listJobs(): List<JobStatusDto> {
        val lastExecutions = jobExecutionRepository.findAll().associateBy { it.jobIdentity }
        return scheduler.scheduledJobs
            .sortedBy { it.id }
            .map { trigger ->
                val managedJob = jobRunner.find(trigger.id)
                JobStatusDto(
                    identity = trigger.id,
                    displayName = managedJob?.displayName,
                    schedule = managedJob?.schedule,
                    nextFireTime = trigger.nextFireTime?.toLocalDateTime(),
                    paused = scheduler.isPaused(trigger.id),
                    runnable = managedJob != null,
                    lastExecution = lastExecutions[trigger.id]?.toDto(),
                )
            }
    }

    @POST
    @Path("/{identity}/run")
    suspend fun runJob(@PathParam("identity") identity: String): Response {
        val job = jobRunner.find(identity) ?: return notFound(identity)
        val result = jobRunner.runNow(job)
        return Response.ok(
            JobExecutionDto(
                runAt = LocalDateTime.now(),
                status = result.status,
                durationMs = result.durationMs,
                error = result.error,
                manual = true,
            )
        ).build()
    }

    @POST
    @Path("/{identity}/pause")
    fun pauseJob(@PathParam("identity") identity: String): Response {
        if (!isScheduled(identity)) return notFound(identity)
        scheduler.pause(identity)
        return Response.noContent().build()
    }

    @POST
    @Path("/{identity}/resume")
    fun resumeJob(@PathParam("identity") identity: String): Response {
        if (!isScheduled(identity)) return notFound(identity)
        scheduler.resume(identity)
        return Response.noContent().build()
    }

    private fun isScheduled(identity: String) = scheduler.scheduledJobs.any { it.id == identity }

    private fun notFound(identity: String) =
        Response.status(Response.Status.NOT_FOUND).entity(mapOf("error" to "unknown job '$identity'")).build()

    private fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())

    private fun JobExecutionEntity.toDto() =
        JobExecutionDto(lastRunAt, lastStatus, lastDurationMs, lastError, manual)
}
