package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.hoohoot.homelab.manager.cleanup.domain.CampaignState
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "cleanup_campaign")
class CleanupCampaignEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "status", nullable = false)
    lateinit var status: String

    @Column(name = "trigger_type", nullable = false)
    lateinit var triggerType: String

    @Column(name = "disk_path", nullable = false)
    lateinit var diskPath: String

    @Column(name = "free_bytes_at_start", nullable = false)
    var freeBytesAtStart: Long = 0

    @Column(name = "threshold_bytes", nullable = false)
    var thresholdBytes: Long = 0

    @Column(name = "target_bytes_to_free", nullable = false)
    var targetBytesToFree: Long = 0

    @Column(name = "freed_bytes", nullable = false)
    var freedBytes: Long = 0

    @Column(name = "grace_ends_at", nullable = false)
    lateinit var graceEndsAt: LocalDateTime

    @Column(name = "announcement_event_id")
    var announcementEventId: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "state", nullable = false)
    var state: CampaignState = CampaignState()

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null

    companion object : PanacheCompanionBase<CleanupCampaignEntity, UUID> {
        const val STATUS_ANNOUNCED = "ANNOUNCED"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}
