package org.hoohoot.homelab.manager.corrector.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import java.time.LocalDateTime

@ApplicationScoped
class CompleteAwaitingWorkflows(private val workflows: CorrectorWorkflows) {
    suspend operator fun invoke(movieIdToImportedAt: Map<Int, LocalDateTime>): Int {
        val completed = workflows.completeAwaitingForMovies(movieIdToImportedAt)
        if (completed > 0) {
            Log.info("El Corrector: $completed workflow(s) completed after Radarr import")
        }
        return completed
    }
}
