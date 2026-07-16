package org.hoohoot.homelab.manager.problems.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.isForceableRejection
import org.hoohoot.homelab.manager.problems.domain.ports.ImportQueue
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows

@ApplicationScoped
class ForceBlockedImports(
    private val workflows: ProblemWorkflows,
    private val importQueue: ImportQueue,
) {
    suspend operator fun invoke(): Int {
        val awaitingMovieIds = workflows.listAwaitingImport()
            .filter { it.state.importForcedAt == null }
            .mapNotNull { it.radarrMovieId }
            .toSet()
        if (awaitingMovieIds.isEmpty()) return 0

        val blockedImports = importQueue.blockedImports()
            .filter { it.radarrMovieId in awaitingMovieIds }
            .filter { blocked -> blocked.statusMessages.any(::isForceableRejection) }
            .distinctBy { it.downloadId }

        val forcedMovieIds = mutableSetOf<Int>()
        for (blocked in blockedImports) {
            try {
                if (importQueue.forceImport(blocked.downloadId, blocked.radarrMovieId)) {
                    Log.info("Problems: forced import of '${blocked.title}' (movie ${blocked.radarrMovieId})")
                    forcedMovieIds += blocked.radarrMovieId
                }
            } catch (exception: Exception) {
                Log.error("Problems: force import failed for download ${blocked.downloadId}", exception)
            }
        }
        if (forcedMovieIds.isNotEmpty()) {
            workflows.markImportForced(forcedMovieIds)
        }
        return forcedMovieIds.size
    }
}
