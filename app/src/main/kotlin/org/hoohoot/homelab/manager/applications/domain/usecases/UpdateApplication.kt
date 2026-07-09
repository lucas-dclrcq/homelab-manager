package org.hoohoot.homelab.manager.applications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.applications.domain.ApplicationInput
import org.hoohoot.homelab.manager.applications.domain.ports.Applications
import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class UpdateApplication(private val applications: Applications) {
    suspend operator fun invoke(id: UUID, input: ApplicationInput): ApplicationEntity? =
        applications.update(id) { entity ->
            entity.applyFields(input)
            input.managedBy?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.managedBy = it }
            input.externalId?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.externalId = it }
            input.logo?.let {
                entity.logo = it.bytes
                entity.logoContentType = it.contentType
            }
            entity.updatedAt = LocalDateTime.now()
        }
}
