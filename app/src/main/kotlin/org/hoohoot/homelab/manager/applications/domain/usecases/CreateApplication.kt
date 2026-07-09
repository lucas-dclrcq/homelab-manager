package org.hoohoot.homelab.manager.applications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.applications.domain.ApplicationInput
import org.hoohoot.homelab.manager.applications.domain.ports.Applications
import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CreateApplication(private val applications: Applications) {
    suspend operator fun invoke(input: ApplicationInput): ApplicationEntity {
        val entity = ApplicationEntity()
        entity.id = UUID.randomUUID()
        entity.applyFields(input)
        entity.managedBy = input.managedBy?.trim()?.takeIf { it.isNotEmpty() }
        entity.externalId = input.externalId?.trim()?.takeIf { it.isNotEmpty() }
        entity.logo = input.logo?.bytes
        entity.logoContentType = input.logo?.contentType
        entity.createdAt = LocalDateTime.now()
        return applications.save(entity)
    }
}

internal fun ApplicationEntity.applyFields(input: ApplicationInput) {
    name = input.name.trim()
    category = input.category.trim()
    description = input.description.trim()
    url = input.url.trim()
    requiresVpn = input.requiresVpn
}
