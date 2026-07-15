package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import java.util.UUID

sealed interface EntryDeleteResult {
    data object Deleted : EntryDeleteResult
    data object NotFound : EntryDeleteResult
    data object RecurringForbidden : EntryDeleteResult
}

@ApplicationScoped
class DeleteFinanceEntry(
    private val financeEntries: FinanceEntries,
) {
    suspend operator fun invoke(id: UUID): EntryDeleteResult {
        val entity = financeEntries.findById(id) ?: return EntryDeleteResult.NotFound
        if (entity.source == EntrySource.RECURRING) return EntryDeleteResult.RecurringForbidden
        financeEntries.delete(id)
        return EntryDeleteResult.Deleted
    }
}
