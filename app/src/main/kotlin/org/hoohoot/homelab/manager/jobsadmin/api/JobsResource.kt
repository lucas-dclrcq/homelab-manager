package org.hoohoot.homelab.manager.jobsadmin.api

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
import org.hoohoot.homelab.manager.shared.api.notFound
import org.hoohoot.homelab.manager.jobs.JobExecutionEntity
import org.hoohoot.homelab.manager.jobs.JobExecutionRepository
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
        val scheduled = scheduler.scheduledJobs
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
        // Jobs à déclenchement manuel uniquement (ex : import Jellystat), inconnus du scheduler
        val manualOnly = jobRunner.all()
            .filter { job -> !isScheduled(job.identity) }
            .sortedBy { it.identity }
            .map { job ->
                JobStatusDto(
                    identity = job.identity,
                    displayName = job.displayName,
                    schedule = job.schedule,
                    nextFireTime = null,
                    paused = false,
                    runnable = true,
                    lastExecution = lastExecutions[job.identity]?.toDto(),
                )
            }
        return scheduled + manualOnly
    }

    @POST
    @Path("/{identity}/run")
    suspend fun runJob(@PathParam("identity") identity: String): Response {
        val job = jobRunner.find(identity) ?: return notFound("unknown job '$identity'")
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
        if (!isScheduled(identity)) return notFound("unknown job '$identity'")
        scheduler.pause(identity)
        return Response.noContent().build()
    }

    @POST
    @Path("/{identity}/resume")
    fun resumeJob(@PathParam("identity") identity: String): Response {
        if (!isScheduled(identity)) return notFound("unknown job '$identity'")
        scheduler.resume(identity)
        return Response.noContent().build()
    }

    private fun isScheduled(identity: String) = scheduler.scheduledJobs.any { it.id == identity }

    private fun Instant.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneId.systemDefault())

    private fun JobExecutionEntity.toDto() =
        JobExecutionDto(lastRunAt, lastStatus, lastDurationMs, lastError, manual)
}
