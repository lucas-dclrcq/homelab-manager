package org.hoohoot.homelab.manager.corrector.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.CorrectorResult
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.domain.ports.Releases
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import org.hoohoot.homelab.manager.corrector.infra.GrabbedRelease
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
    private val workflows: CorrectorWorkflows,
    private val releases: Releases,
) {
    suspend operator fun invoke(id: UUID, username: String, request: GrabRequest): CorrectorResult {
        val workflow = workflows.findForUser(id, username) ?: return CorrectorResult.NotFound
        if (workflow.status != CorrectorWorkflowEntity.STATUS_IN_PROGRESS || workflow.problemType == null) {
            return CorrectorResult.Conflict("workflow must be in progress with a problem selected")
        }

        try {
            releases.grab(request.guid, request.indexerId)
        } catch (exception: Exception) {
            Log.error("El Corrector: grab failed for workflow $id", exception)
            return CorrectorResult.GrabFailed
        }

        val updated = workflows.update(id, username) { entity ->
            entity.status = CorrectorWorkflowEntity.STATUS_AWAITING_IMPORT
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
        } ?: return CorrectorResult.NotFound
        return CorrectorResult.Ok(updated)
    }
}
