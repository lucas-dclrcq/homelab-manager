package org.hoohoot.homelab.manager.applications.api

import org.hoohoot.homelab.manager.applications.infra.ApplicationEntity
import org.hoohoot.homelab.manager.applications.infra.ApplicationSummary
import java.time.LocalDateTime
import java.util.UUID

data class ApplicationDto(
    val id: UUID,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val hasLogo: Boolean,
    val managedBy: String?,
    val externalId: String?,
    val updatedAt: LocalDateTime?,
)

fun ApplicationSummary.toDto() =
    ApplicationDto(id, name, category, description, url, requiresVpn, hasLogo, managedBy, externalId, updatedAt)

fun ApplicationEntity.toDto() =
    ApplicationDto(requireNotNull(id), name, category, description, url, requiresVpn, logo != null, managedBy, externalId, updatedAt)
