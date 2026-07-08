package org.hoohoot.homelab.manager.corrector.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.corrector.domain.CorrectorResult
import org.hoohoot.homelab.manager.corrector.domain.ports.CorrectorWorkflows
import org.hoohoot.homelab.manager.corrector.infra.CorrectorWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CreateWorkflow(private val workflows: CorrectorWorkflows) {
    suspend operator fun invoke(username: String, mediaType: String?): CorrectorResult {
        if (mediaType != CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE) {
            return CorrectorResult.Invalid("mediaType must be '${CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE}'")
        }
        val entity = CorrectorWorkflowEntity()
        entity.id = UUID.randomUUID()
        entity.username = username
        entity.mediaType = CorrectorWorkflowEntity.MEDIA_TYPE_MOVIE
        entity.status = CorrectorWorkflowEntity.STATUS_IN_PROGRESS
        entity.createdAt = LocalDateTime.now()
        entity.updatedAt = LocalDateTime.now()
        return CorrectorResult.Ok(workflows.save(entity))
    }
}
