package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import org.hoohoot.homelab.manager.finances.infra.RecurringRuleEntity

@ApplicationScoped
class ListRecurringRules(
    private val recurringRules: RecurringRules,
) {
    suspend operator fun invoke(): List<RecurringRuleEntity> = recurringRules.listAll()
}
