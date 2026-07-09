package org.hoohoot.homelab.manager.finances.api

import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.ports.EntriesPage
import org.hoohoot.homelab.manager.finances.domain.ports.MonthlyTotals
import org.hoohoot.homelab.manager.finances.domain.ports.YearlySummary
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryView
import org.hoohoot.homelab.manager.finances.infra.FinanceSettingsEntity
import org.hoohoot.homelab.manager.finances.infra.RecurringRuleEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class FinanceSummaryDto(
    val year: Int,
    val totalContributionsCents: Long,
    val totalExpensesCents: Long,
    val balanceCents: Long,
)

data class MonthlyTotalsDto(
    val month: Int,
    val contributionsCents: Long,
    val expensesCents: Long,
)

data class FinanceEntryDto(
    val id: UUID,
    val type: EntryType,
    val source: EntrySource,
    val label: String,
    val vendor: String?,
    val amountCents: Int,
    val entryDate: LocalDate,
    val memberId: UUID?,
    val memberDisplayName: String?,
    val period: String?,
    val notes: String?,
)

data class FinanceEntriesPageDto(
    val items: List<FinanceEntryDto>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

data class RecurringRuleDto(
    val id: UUID,
    val type: EntryType,
    val label: String,
    val amountCents: Int,
    val dayOfMonth: Int,
    val memberId: UUID?,
    val vendor: String?,
    val active: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate?,
)

data class FinanceSettingsDto(
    val kwhPrice: BigDecimal?,
)

data class EnergyStatusDto(
    val currentPowerWatts: Double?,
    val estimatedMonthlyKwh: Double?,
    val estimatedMonthlyCostCents: Long?,
    val kwhPriceConfigured: Boolean,
)

data class FinanceEntryRequest(
    val type: EntryType?,
    val label: String?,
    val vendor: String?,
    val amountCents: Int?,
    val entryDate: LocalDate?,
    val memberId: UUID?,
    val notes: String?,
)

data class RecurringRuleRequest(
    val type: EntryType?,
    val label: String?,
    val amountCents: Int?,
    val dayOfMonth: Int?,
    val memberId: UUID?,
    val vendor: String?,
    val active: Boolean?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
)

data class FinanceSettingsRequest(
    val kwhPrice: BigDecimal?,
)

fun YearlySummary.toDto(year: Int) = FinanceSummaryDto(
    year = year,
    totalContributionsCents = contributionsCents,
    totalExpensesCents = expensesCents,
    balanceCents = contributionsCents - expensesCents,
)

fun MonthlyTotals.toDto() = MonthlyTotalsDto(month, contributionsCents, expensesCents)

fun FinanceEntryView.toDto() = FinanceEntryDto(
    id = id,
    type = type,
    source = source,
    label = label,
    vendor = vendor,
    amountCents = amountCents,
    entryDate = entryDate,
    memberId = memberId,
    memberDisplayName = memberDisplayName,
    period = period,
    notes = notes,
)

fun FinanceEntryEntity.toDto(memberDisplayName: String? = null) = FinanceEntryDto(
    id = id!!,
    type = type,
    source = source,
    label = label,
    vendor = vendor,
    amountCents = amountCents,
    entryDate = entryDate,
    memberId = memberId,
    memberDisplayName = memberDisplayName,
    period = period,
    notes = notes,
)

fun EntriesPage.toDto() = FinanceEntriesPageDto(
    items = items.map { it.toDto() },
    total = total,
    page = page,
    pageSize = pageSize,
)

fun RecurringRuleEntity.toDto() = RecurringRuleDto(
    id = id!!,
    type = type,
    label = label,
    amountCents = amountCents,
    dayOfMonth = dayOfMonth,
    memberId = memberId,
    vendor = vendor,
    active = active,
    startDate = startDate,
    endDate = endDate,
)

fun FinanceSettingsEntity.toDto() = FinanceSettingsDto(kwhPrice = kwhPrice)
