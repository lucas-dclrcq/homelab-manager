package org.hoohoot.homelab.manager.jobs

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Instance
import org.hoohoot.homelab.manager.jobs.JobExecutionEntity
import org.hoohoot.homelab.manager.jobs.JobExecutionRepository

data class JobRunResult(
    val status: String,
    val durationMs: Long,
    val error: String?,
)

/**
 * Runs a [ManagedJob] and records the execution in job_execution.
 * Both scheduled fires and manual runs go through here: the scheduler's
 * SuccessfulExecution/FailedExecution CDI events are not fired reliably
 * for suspend methods in dev mode, so recording is done in-line instead.
 */
@ApplicationScoped
class JobRunner(
    private val managedJobs: Instance<ManagedJob>,
    private val jobExecutionRepository: JobExecutionRepository,
) {
    fun find(identity: String): ManagedJob? = managedJobs.firstOrNull { it.identity == identity }

    fun all(): List<ManagedJob> = managedJobs.toList()

    suspend fun runNow(job: ManagedJob): JobRunResult = run(job, manual = true)

    suspend fun runScheduled(job: ManagedJob): JobRunResult = run(job, manual = false)

    private suspend fun run(job: ManagedJob, manual: Boolean): JobRunResult {
        val start = System.nanoTime()
        val result = try {
            job.execute()
            JobRunResult(JobExecutionEntity.STATUS_SUCCESS, elapsedMs(start), null)
        } catch (e: Exception) {
            Log.error("Run of job '${job.identity}' failed", e)
            JobRunResult(JobExecutionEntity.STATUS_FAILURE, elapsedMs(start), e.message ?: e.toString())
        }
        try {
            jobExecutionRepository.record(job.identity, result.status, result.durationMs, result.error, manual)
        } catch (e: Exception) {
            Log.error("Failed to record execution of job '${job.identity}'", e)
        }
        return result
    }

    private fun elapsedMs(startNanos: Long) = (System.nanoTime() - startNanos) / 1_000_000
}
