package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CreateWorkflow(private val workflows: ProblemWorkflows) {
    suspend operator fun invoke(username: String, mediaType: String?): ProblemResult {
        if (mediaType != ProblemWorkflowEntity.MEDIA_TYPE_MOVIE) {
            return ProblemResult.Invalid("mediaType must be '${ProblemWorkflowEntity.MEDIA_TYPE_MOVIE}'")
        }
        val entity = ProblemWorkflowEntity()
        entity.id = UUID.randomUUID()
        entity.username = username
        entity.mediaType = ProblemWorkflowEntity.MEDIA_TYPE_MOVIE
        entity.status = ProblemWorkflowEntity.STATUS_IN_PROGRESS
        entity.createdAt = LocalDateTime.now()
        entity.updatedAt = LocalDateTime.now()
        return ProblemResult.Ok(workflows.save(entity))
    }
}
