package org.hoohoot.homelab.manager.leader

import io.quarkus.scheduler.Scheduled.SkipPredicate
import io.quarkus.scheduler.ScheduledExecution
import jakarta.inject.Singleton

@Singleton
class NotLeader(private val leaderElection: LeaderElectionService) : SkipPredicate {
    override fun test(execution: ScheduledExecution): Boolean = !leaderElection.isLeader
}
