package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.RecurringRuleInput
import org.hoohoot.homelab.manager.members.domain.ports.Members
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import java.util.UUID

@ApplicationScoped
class UpdateRecurringRule(
    private val recurringRules: RecurringRules,
    private val members: Members,
) {
    suspend operator fun invoke(id: UUID, input: RecurringRuleInput): RuleWriteResult {
        val memberId = input.memberId.takeIf { input.type == EntryType.CONTRIBUTION }
        if (memberId != null && members.findById(memberId) == null) return RuleWriteResult.UnknownMember

        val updated = recurringRules.update(id) { entity ->
            entity.type = input.type
            entity.label = input.label
            entity.amountCents = input.amountCents
            entity.dayOfMonth = input.dayOfMonth
            entity.memberId = memberId
            entity.vendor = input.vendor
            entity.active = input.active
            entity.startDate = input.startDate
            entity.endDate = input.endDate
        } ?: return RuleWriteResult.NotFound

        return RuleWriteResult.Ok(updated)
    }
}
