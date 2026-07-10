package org.hoohoot.homelab.manager.problems.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.Accessor
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.domain.ports.Releases
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import org.hoohoot.homelab.manager.problems.infra.GrabbedRelease
import java.time.LocalDateTime
import java.util.UUID

data class GrabRequest(
    val guid: String,
    val indexerId: Int,
    val title: String?,
    val indexer: String?,
    val quality: String?,
    val size: Long?,
)

@ApplicationScoped
class GrabRelease(
    private val workflows: ProblemWorkflows,
    private val releases: Releases,
) {
    suspend operator fun invoke(id: UUID, accessor: Accessor, request: GrabRequest): ProblemResult {
        val workflow = workflows.find(id, accessor) ?: return ProblemResult.NotFound
        if (workflow.status != ProblemWorkflowEntity.STATUS_IN_PROGRESS || workflow.problemType == null) {
            return ProblemResult.Conflict("workflow must be in progress with a problem selected")
        }

        try {
            releases.grab(request.guid, request.indexerId)
        } catch (exception: Exception) {
            Log.error("Problems: grab failed for workflow $id", exception)
            return ProblemResult.GrabFailed
        }

        val updated = workflows.update(id, accessor) { entity ->
            entity.status = ProblemWorkflowEntity.STATUS_AWAITING_IMPORT
            entity.grabbedAt = LocalDateTime.now()
            entity.state = entity.state.copy(
                grabbedRelease = GrabbedRelease(
                    guid = request.guid,
                    indexerId = request.indexerId,
                    indexer = request.indexer,
                    title = request.title,
                    quality = request.quality,
                    size = request.size,
                ),
            )
        } ?: return ProblemResult.NotFound
        return ProblemResult.Ok(updated)
    }
}
