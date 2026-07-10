package org.hoohoot.homelab.manager.problems.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import java.time.LocalDateTime

@ApplicationScoped
class CompleteAwaitingWorkflows(private val workflows: ProblemWorkflows) {
    suspend operator fun invoke(movieIdToImportedAt: Map<Int, LocalDateTime>): Int {
        val completed = workflows.completeAwaitingForMovies(movieIdToImportedAt)
        if (completed > 0) {
            Log.info("Problems: $completed workflow(s) completed after Radarr import")
        }
        return completed
    }
}
