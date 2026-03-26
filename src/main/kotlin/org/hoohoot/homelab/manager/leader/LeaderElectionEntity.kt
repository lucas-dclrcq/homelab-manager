package org.hoohoot.homelab.manager.leader

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

enum class LeaderStatus {
    ACTIVE, RELEASED
}

@Entity
@Table(name = "leader_election")
class LeaderElectionEntity : PanacheEntityBase {
    @Id
    @Column(name = "lock_key")
    lateinit var lockKey: String

    @Column(name = "instance_id", nullable = false)
    lateinit var instanceId: String

    @Column(name = "elected_at", nullable = false)
    lateinit var electedAt: LocalDateTime

    @Column(name = "last_heartbeat", nullable = false)
    lateinit var lastHeartbeat: LocalDateTime

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: LeaderStatus = LeaderStatus.ACTIVE

    companion object : PanacheCompanionBase<LeaderElectionEntity, String>
}
