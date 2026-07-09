package org.hoohoot.homelab.manager.applications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.applications.domain.ApplicationInput
import org.hoohoot.homelab.manager.applications.domain.LogoChange
import org.hoohoot.homelab.manager.applications.domain.ports.Applications
import org.hoohoot.homelab.manager.applications.domain.ports.LogoFetcher
import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class CreateApplication(
    private val applications: Applications,
    private val logoFetcher: LogoFetcher,
) {
    suspend operator fun invoke(input: ApplicationInput): ApplicationEntity {
        val entity = ApplicationEntity()
        entity.id = UUID.randomUUID()
        entity.applyFields(input)
        entity.managedBy = input.managedBy?.trim()?.takeIf { it.isNotEmpty() }
        entity.externalId = input.externalId?.trim()?.takeIf { it.isNotEmpty() }
        when (val change = input.logo) {
            is LogoChange.Upload -> {
                entity.logo = change.logo.bytes
                entity.logoContentType = change.logo.contentType
            }

            // Échec de téléchargement : l'app est créée sans logo ni source, le prochain sweep
            // de l'opérateur verra le drift et retentera
            is LogoChange.FromUrl -> logoFetcher.fetch(change.url)?.let {
                entity.logo = it.bytes
                entity.logoContentType = it.contentType
                entity.logoSourceUrl = change.url
            }

            LogoChange.Keep, LogoChange.Remove -> {}
        }
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
