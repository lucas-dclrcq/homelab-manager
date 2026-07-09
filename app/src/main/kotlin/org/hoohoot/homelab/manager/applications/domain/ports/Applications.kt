package org.hoohoot.homelab.manager.applications.domain.ports

import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import org.hoohoot.homelab.manager.applications.infra.ApplicationSummary
import java.util.UUID

interface Applications {
    suspend fun listSummaries(): List<ApplicationSummary>
    suspend fun findById(id: UUID): ApplicationEntity?
    suspend fun save(entity: ApplicationEntity): ApplicationEntity
    suspend fun update(id: UUID, mutate: (ApplicationEntity) -> Unit): ApplicationEntity?
    suspend fun delete(id: UUID): Boolean
}
