package org.hoohoot.homelab.manager.applications.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.applications.domain.ports.Applications
import java.util.UUID

@ApplicationScoped
class DeleteApplication(private val applications: Applications) {
    suspend operator fun invoke(id: UUID): Boolean = applications.delete(id)
}
