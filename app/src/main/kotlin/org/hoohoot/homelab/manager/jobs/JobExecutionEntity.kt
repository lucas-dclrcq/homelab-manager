package org.hoohoot.homelab.manager.jobs

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "job_execution")
class JobExecutionEntity : PanacheEntityBase {
    @Id
    @Column(name = "job_identity")
    lateinit var jobIdentity: String

    @Column(name = "last_run_at", nullable = false)
    lateinit var lastRunAt: LocalDateTime

    @Column(name = "last_status", nullable = false)
    lateinit var lastStatus: String

    @Column(name = "last_duration_ms")
    var lastDurationMs: Long? = null

    @Column(name = "last_error")
    var lastError: String? = null

    @Column(name = "manual", nullable = false)
    var manual: Boolean = false

    companion object : PanacheCompanionBase<JobExecutionEntity, String> {
        const val STATUS_SUCCESS = "SUCCESS"
        const val STATUS_FAILURE = "FAILURE"
    }
}
