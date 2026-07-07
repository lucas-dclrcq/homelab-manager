package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

@ApplicationScoped
class JobExecutionRepository {

    fun recordUni(
        jobIdentity: String,
        status: String,
        durationMs: Long?,
        error: String?,
        manual: Boolean,
    ): Uni<Void> =
        Panache.withTransaction {
            JobExecutionEntity.findById(jobIdentity).chain { existing ->
                val entity = existing ?: JobExecutionEntity().also { it.jobIdentity = jobIdentity }
                entity.lastRunAt = LocalDateTime.now()
                entity.lastStatus = status
                entity.lastDurationMs = durationMs
                entity.lastError = error
                entity.manual = manual
                entity.persist<JobExecutionEntity>()
            }
        }.replaceWithVoid()

    suspend fun record(
        jobIdentity: String,
        status: String,
        durationMs: Long?,
        error: String?,
        manual: Boolean,
    ) {
        recordUni(jobIdentity, status, durationMs, error, manual).awaitSuspending()
    }

    suspend fun findAll(): List<JobExecutionEntity> =
        Panache.withSession { JobExecutionEntity.listAll() }.awaitSuspending()

    suspend fun findByIdentity(jobIdentity: String): JobExecutionEntity? =
        Panache.withSession { JobExecutionEntity.findById(jobIdentity) }.awaitSuspending()
}
