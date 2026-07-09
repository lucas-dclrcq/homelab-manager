package org.hoohoot.homelab.manager.finances.domain.ports

import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryView
import java.util.UUID

data class YearlySummary(val contributionsCents: Long, val expensesCents: Long)

data class MonthlyTotals(val month: Int, val contributionsCents: Long, val expensesCents: Long)

data class EntriesPage(val items: List<FinanceEntryView>, val total: Long, val page: Int, val pageSize: Int)

interface FinanceEntries {
    suspend fun search(year: Int, type: EntryType?, page: Int, pageSize: Int): EntriesPage
    suspend fun yearlySummary(year: Int): YearlySummary
    suspend fun monthlyBreakdown(year: Int): List<MonthlyTotals>
    suspend fun findById(id: UUID): FinanceEntryEntity?
    suspend fun save(entity: FinanceEntryEntity): FinanceEntryEntity
    suspend fun saveIfAbsent(entity: FinanceEntryEntity): Boolean
    suspend fun update(id: UUID, mutate: (FinanceEntryEntity) -> Unit): FinanceEntryEntity?
    suspend fun delete(id: UUID): Boolean
    suspend fun energyPeriodExists(period: String): Boolean
}
