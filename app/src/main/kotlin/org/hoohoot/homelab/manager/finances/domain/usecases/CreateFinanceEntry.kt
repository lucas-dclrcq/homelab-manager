package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.FinanceEntryInput
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.members.domain.ports.Members
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import java.time.LocalDateTime
import java.util.UUID

sealed interface EntryWriteResult {
    data class Ok(val entity: FinanceEntryEntity) : EntryWriteResult
    data object UnknownMember : EntryWriteResult
    data object NotFound : EntryWriteResult
}

@ApplicationScoped
class CreateFinanceEntry(
    private val financeEntries: FinanceEntries,
    private val members: Members,
) {
    suspend operator fun invoke(input: FinanceEntryInput): EntryWriteResult {
        val memberId = input.memberId.takeIf { input.type == EntryType.CONTRIBUTION }
        if (memberId != null && members.findById(memberId) == null) return EntryWriteResult.UnknownMember

        val entity = FinanceEntryEntity().apply {
            id = UUID.randomUUID()
            type = input.type
            source = EntrySource.MANUAL
            label = input.label
            vendor = input.vendor
            amountCents = input.amountCents
            entryDate = input.entryDate
            this.memberId = memberId
            notes = input.notes
            createdAt = LocalDateTime.now()
        }
        return EntryWriteResult.Ok(financeEntries.save(entity))
    }
}
