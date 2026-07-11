package org.hoohoot.homelab.manager.cleanup.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.Accessor
import org.hoohoot.homelab.manager.cleanup.domain.UnprotectResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.Protections
import java.util.UUID

@ApplicationScoped
class UnprotectMedia(
    private val protections: Protections,
) {
    suspend operator fun invoke(protectionId: UUID, accessor: Accessor): UnprotectResult {
        val protection = protections.find(protectionId) ?: return UnprotectResult.NotFound

        val allowed = when (accessor) {
            is Accessor.Admin -> true
            is Accessor.User -> protection.protectedBy == accessor.username
        }
        if (!allowed) return UnprotectResult.Forbidden

        protections.delete(protectionId)
        return UnprotectResult.Ok
    }
}
