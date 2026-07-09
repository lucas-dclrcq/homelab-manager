package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.FinanceEntryInput
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.members.domain.ports.Members
import java.util.UUID

@ApplicationScoped
class UpdateFinanceEntry(
    private val financeEntries: FinanceEntries,
    private val members: Members,
) {
    suspend operator fun invoke(id: UUID, input: FinanceEntryInput): EntryWriteResult {
        val memberId = input.memberId.takeIf { input.type == EntryType.CONTRIBUTION }
        if (memberId != null && members.findById(memberId) == null) return EntryWriteResult.UnknownMember

        val updated = financeEntries.update(id) { entity ->
            entity.type = input.type
            entity.label = input.label
            entity.vendor = input.vendor
            entity.amountCents = input.amountCents
            entity.entryDate = input.entryDate
            entity.memberId = memberId
            entity.notes = input.notes
        } ?: return EntryWriteResult.NotFound

        return EntryWriteResult.Ok(updated)
    }
}
