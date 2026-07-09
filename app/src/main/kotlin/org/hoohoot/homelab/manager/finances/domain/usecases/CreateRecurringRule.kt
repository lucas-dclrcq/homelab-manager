package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.RecurringRuleInput
import org.hoohoot.homelab.manager.members.domain.ports.Members
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import org.hoohoot.homelab.manager.finances.infra.RecurringRuleEntity
import java.time.LocalDateTime
import java.util.UUID

sealed interface RuleWriteResult {
    data class Ok(val entity: RecurringRuleEntity) : RuleWriteResult
    data object UnknownMember : RuleWriteResult
    data object NotFound : RuleWriteResult
}

@ApplicationScoped
class CreateRecurringRule(
    private val recurringRules: RecurringRules,
    private val members: Members,
) {
    suspend operator fun invoke(input: RecurringRuleInput): RuleWriteResult {
        val memberId = input.memberId.takeIf { input.type == EntryType.CONTRIBUTION }
        if (memberId != null && members.findById(memberId) == null) return RuleWriteResult.UnknownMember

        val entity = RecurringRuleEntity().apply {
            id = UUID.randomUUID()
            type = input.type
            label = input.label
            amountCents = input.amountCents
            dayOfMonth = input.dayOfMonth
            this.memberId = memberId
            vendor = input.vendor
            active = input.active
            startDate = input.startDate
            endDate = input.endDate
            createdAt = LocalDateTime.now()
        }
        return RuleWriteResult.Ok(recurringRules.save(entity))
    }
}
