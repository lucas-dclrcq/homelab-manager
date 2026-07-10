package org.hoohoot.homelab.manager.problems.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.problems.domain.ProblemResult
import org.hoohoot.homelab.manager.problems.domain.ports.ProblemWorkflows
import org.hoohoot.homelab.manager.problems.infra.ProblemWorkflowEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CreateWorkflow(private val workflows: ProblemWorkflows) {
    companion object {
        private val MEDIA_TYPES = setOf(
            ProblemWorkflowEntity.MEDIA_TYPE_MOVIE,
            ProblemWorkflowEntity.MEDIA_TYPE_TV,
        )
    }

    suspend operator fun invoke(username: String, mediaType: String?): ProblemResult {
        if (mediaType !in MEDIA_TYPES) {
            return ProblemResult.Invalid("mediaType must be one of ${MEDIA_TYPES.joinToString("', '", "'", "'")}")
        }
        val entity = ProblemWorkflowEntity()
        entity.id = UUID.randomUUID()
        entity.username = username
        entity.mediaType = requireNotNull(mediaType)
        entity.status = ProblemWorkflowEntity.STATUS_IN_PROGRESS
        entity.createdAt = LocalDateTime.now()
        entity.updatedAt = LocalDateTime.now()
        return ProblemResult.Ok(workflows.save(entity))
    }
}
