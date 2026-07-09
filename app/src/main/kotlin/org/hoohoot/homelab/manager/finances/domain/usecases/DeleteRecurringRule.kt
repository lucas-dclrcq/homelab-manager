package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import java.util.UUID

@ApplicationScoped
class DeleteRecurringRule(
    private val recurringRules: RecurringRules,
) {
    suspend operator fun invoke(id: UUID): Boolean = recurringRules.delete(id)
}
