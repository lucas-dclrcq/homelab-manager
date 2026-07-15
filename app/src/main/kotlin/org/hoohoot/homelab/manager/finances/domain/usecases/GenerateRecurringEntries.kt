package org.hoohoot.homelab.manager.finances.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

@ApplicationScoped
class GenerateRecurringEntries(
    private val recurringRules: RecurringRules,
    private val financeEntries: FinanceEntries,
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Int {
        var created = 0
        val currentPeriod = YearMonth.from(today)

        for (rule in recurringRules.listActive()) {
            var period = YearMonth.from(rule.startDate)
            while (period <= currentPeriod) {
                val dueDate = period.atDay(rule.dayOfMonth)
                if (dueDate.isBefore(rule.startDate)) {
                    period = period.plusMonths(1)
                    continue
                }
                val endDate = rule.endDate
                if (endDate != null && dueDate.isAfter(endDate)) break
                if (dueDate.isAfter(today)) break

                val entry = FinanceEntryEntity().apply {
                    id = UUID.randomUUID()
                    type = rule.type
                    source = EntrySource.RECURRING
                    label = rule.label
                    vendor = rule.vendor
                    amountCents = rule.amountCents
                    entryDate = dueDate
                    memberId = rule.memberId
                    ruleId = rule.id
                    this.period = period.toString()
                    createdAt = LocalDateTime.now()
                }
                if (financeEntries.saveIfAbsent(entry)) created++
                period = period.plusMonths(1)
            }
        }

        if (created > 0) Log.info("Recurring finance entries: $created écriture(s) générée(s)")
        return created
    }
}
