package org.hoohoot.homelab.manager.leader

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.logging.Log
import io.quarkus.runtime.ShutdownEvent
import io.quarkus.scheduler.Scheduled
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import jakarta.enterprise.event.Event
import jakarta.enterprise.event.Observes
import java.time.LocalDateTime

@ApplicationScoped
class LeaderElectionService(
    config: LeaderElectionConfiguration,
    private val leadershipAcquired: Event<LeadershipAcquired>,
    private val leadershipLost: Event<LeadershipLost>
) {

    val instanceId: String = config.instanceId().orElseGet { java.util.UUID.randomUUID().toString() }

    @Volatile
    private var _isLeader: Boolean = false

    val isLeader: Boolean get() = _isLeader

    @Scheduled(every = "10s", delayed = "1s")
    @ActivateRequestContext
    suspend fun heartbeat() {
        try {
            val now = LocalDateTime.now()
            val expiry = now.minusSeconds(30)

            val renewed = Panache.withTransaction {
                LeaderElectionEntity.update(
                    "lastHeartbeat = ?1 where lockKey = 'MAIN' and instanceId = ?2 and status = ?3",
                    now, instanceId, LeaderStatus.ACTIVE
                )
            }.awaitSuspending()

            if (renewed > 0) {
                if (!_isLeader) {
                    Log.info("Renewed leader lease (instance=$instanceId)")
                    setLeader(true)
                }
                return
            }

            val claimed = Panache.withTransaction {
                LeaderElectionEntity.update(
                    "instanceId = ?1, electedAt = ?2, lastHeartbeat = ?2, status = ?3 where lockKey = 'MAIN' and (status = ?4 or lastHeartbeat < ?5)",
                    instanceId, now, LeaderStatus.ACTIVE, LeaderStatus.RELEASED, expiry
                )
            }.awaitSuspending()

            if (claimed > 0) {
                Log.info("Acquired leader lease (instance=$instanceId)")
                setLeader(true)
            } else if (_isLeader) {
                Log.info("Lost leader lease (instance=$instanceId)")
                setLeader(false)
            }
        } catch (e: Exception) {
            Log.error("Leader election heartbeat failed", e)
            if (_isLeader) setLeader(false)
        }
    }

    fun onStop(@Observes event: ShutdownEvent) {
        try {
            Panache.withTransaction {
                LeaderElectionEntity.update(
                    "status = ?1 where lockKey = 'MAIN' and instanceId = ?2",
                    LeaderStatus.RELEASED, instanceId
                )
            }.await().indefinitely()
            Log.info("Released leader lease (instance=$instanceId)")
        } catch (e: Exception) {
            Log.warn("Failed to release leader lease on shutdown", e)
        }
    }

    suspend fun releaseLeadership() {
        if (!_isLeader) return
        try {
            Panache.withTransaction {
                LeaderElectionEntity.update(
                    "status = ?1 where lockKey = 'MAIN' and instanceId = ?2",
                    LeaderStatus.RELEASED, instanceId
                )
            }.await().indefinitely()
            Log.info("Voluntarily released leader lease (instance=$instanceId)")
        } catch (e: Exception) {
            Log.warn("Failed to release leader lease", e)
        }
        setLeader(false)
    }

    private fun setLeader(leader: Boolean) {
        val wasLeader = _isLeader
        _isLeader = leader
        if (leader && !wasLeader) leadershipAcquired.fire(LeadershipAcquired)
        if (!leader && wasLeader) leadershipLost.fire(LeadershipLost)
    }
}
