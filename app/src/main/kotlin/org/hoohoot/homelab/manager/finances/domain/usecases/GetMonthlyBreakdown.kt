package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.finances.domain.ports.MonthlyTotals

@ApplicationScoped
class GetMonthlyBreakdown(
    private val financeEntries: FinanceEntries,
) {
    suspend operator fun invoke(year: Int): List<MonthlyTotals> {
        val byMonth = financeEntries.monthlyBreakdown(year).associateBy { it.month }
        return (1..12).map { month ->
            byMonth[month] ?: MonthlyTotals(month, contributionsCents = 0, expensesCents = 0)
        }
    }
}
