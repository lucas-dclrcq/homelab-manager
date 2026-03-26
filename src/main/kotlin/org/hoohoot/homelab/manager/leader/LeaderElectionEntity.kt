package org.hoohoot.homelab.manager.leader

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

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

    companion object : PanacheCompanionBase<LeaderElectionEntity, String>
}
