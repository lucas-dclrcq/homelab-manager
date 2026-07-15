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
class UpdateApplication(
    private val applications: Applications,
    private val logoFetcher: LogoFetcher,
) {
    suspend operator fun invoke(id: UUID, input: ApplicationInput): ApplicationEntity? {
        val change = input.logo
        val fetched = if (change is LogoChange.FromUrl && change.url != applications.findById(id)?.logoSourceUrl) {
            logoFetcher.fetch(change.url)
        } else {
            null
        }

        return applications.update(id) { entity ->
            entity.applyFields(input)
            input.managedBy?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.managedBy = it }
            input.externalId?.trim()?.takeIf { it.isNotEmpty() }?.let { entity.externalId = it }
            when (change) {
                is LogoChange.Upload -> {
                    entity.logo = change.logo.bytes
                    entity.logoContentType = change.logo.contentType
                    entity.logoSourceUrl = null
                }

                is LogoChange.FromUrl -> fetched?.let {
                    entity.logo = it.bytes
                    entity.logoContentType = it.contentType
                    entity.logoSourceUrl = change.url
                }

                LogoChange.Remove -> if (entity.logoSourceUrl != null) {
                    entity.logo = null
                    entity.logoContentType = null
                    entity.logoSourceUrl = null
                }

                LogoChange.Keep -> {}
            }
            entity.updatedAt = LocalDateTime.now()
        }
    }
}
