package org.hoohoot.homelab.manager.finances.domain

import java.time.LocalDate
import java.util.UUID

enum class EntryType { CONTRIBUTION, EXPENSE }

enum class EntrySource { MANUAL, RECURRING, ENERGY }

data class FinanceEntryInput(
    val type: EntryType,
    val label: String,
    val vendor: String?,
    val amountCents: Int,
    val entryDate: LocalDate,
    val memberId: UUID?,
    val notes: String?,
)

data class RecurringRuleInput(
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
