package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.ActiveProblemIds
import org.hoohoot.homelab.manager.cleanup.domain.ports.ActiveProblems
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity

@ApplicationScoped
class ProblemWorkflowGuardAdapter : ActiveProblems {
    companion object {
        private val TERMINAL_STATUSES = listOf(
            ProblemWorkflowEntity.STATUS_COMPLETED,
            ProblemWorkflowEntity.STATUS_RESOLVED,
            ProblemWorkflowEntity.STATUS_ABANDONED,
        )
    }

    override suspend fun activeMediaIds(): ActiveProblemIds {
        val active = Panache.withSession {
            ProblemWorkflowEntity.list("status not in ?1", TERMINAL_STATUSES)
        }.awaitSuspending()

        return ActiveProblemIds(
            radarrMovieIds = active.mapNotNull { it.radarrMovieId }.toSet(),
            sonarrSeriesIds = active.mapNotNull { it.sonarrSeriesId }.toSet(),
        )
    }
}
