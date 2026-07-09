package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.finances.domain.ports.YearlySummary

@ApplicationScoped
class GetFinanceSummary(
    private val financeEntries: FinanceEntries,
) {
    suspend operator fun invoke(year: Int): YearlySummary = financeEntries.yearlySummary(year)
}
