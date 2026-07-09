package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.ports.EntriesPage
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries

const val MAX_PAGE_SIZE = 100

@ApplicationScoped
class SearchFinanceEntries(
    private val financeEntries: FinanceEntries,
) {
    suspend operator fun invoke(year: Int, type: EntryType?, page: Int, pageSize: Int): EntriesPage =
        financeEntries.search(
            year = year,
            type = type,
            page = page.coerceAtLeast(0),
            pageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE),
        )
}
