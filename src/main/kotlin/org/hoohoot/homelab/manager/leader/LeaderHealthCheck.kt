package org.hoohoot.homelab.manager.leader

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.health.HealthCheck
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness

@Liveness
@ApplicationScoped
class LeaderHealthCheck(private val leaderElection: LeaderElectionService) : HealthCheck {
    override fun call(): HealthCheckResponse =
        HealthCheckResponse.named("leader-election")
            .up()
            .withData("isLeader", leaderElection.isLeader)
            .withData("instanceId", leaderElection.instanceId)
            .build()
}
