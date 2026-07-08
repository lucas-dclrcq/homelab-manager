package org.hoohoot.homelab.manager.portal.corrector

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.portal.persistence.CorrectorWorkflowRepository
import java.time.LocalDateTime

@ApplicationScoped
class CorrectorCompletionService(
    private val correctorWorkflowRepository: CorrectorWorkflowRepository,
) {
    suspend fun completeAwaitingMovieWorkflows(movieIdToImportedAt: Map<Int, LocalDateTime>): Int {
        val completed = correctorWorkflowRepository.completeAwaitingForMovies(movieIdToImportedAt)
        if (completed > 0) {
            Log.info("El Corrector: $completed workflow(s) completed after Radarr import")
        }
        return completed
    }
}
