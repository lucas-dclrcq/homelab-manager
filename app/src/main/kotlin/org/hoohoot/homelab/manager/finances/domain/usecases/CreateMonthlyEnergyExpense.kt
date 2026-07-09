package org.hoohoot.homelab.manager.finances.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.ports.EnergyMetrics
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceEntries
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceSettings
import org.hoohoot.homelab.manager.finances.infra.FinanceEntryEntity
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID

// Rattrapage limité : au-delà, la rétention Prometheus ne couvre plus les mesures
private const val CATCH_UP_MONTHS = 3

@ApplicationScoped
class CreateMonthlyEnergyExpense(
    private val financeEntries: FinanceEntries,
    private val financeSettings: FinanceSettings,
    private val energyMetrics: EnergyMetrics,
) {
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): Int {
        val kwhPrice = financeSettings.get().kwhPrice
            ?: throw IllegalStateException("Prix du kWh non configuré dans les réglages Finances")

        var created = 0
        for (offset in CATCH_UP_MONTHS downTo 1) {
            val month = YearMonth.from(today).minusMonths(offset.toLong())
            val period = month.toString()
            if (financeEntries.energyPeriodExists(period)) continue

            val zone = ZoneId.systemDefault()
            val from = month.atDay(1).atStartOfDay(zone).toInstant()
            val to = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant()
            val averageWatts = energyMetrics.averagePowerWatts(from, to)
            if (averageWatts == null) {
                // Seul le mois écoulé est bloquant : les mois plus anciens peuvent être
                // sortis de la rétention Prometheus, inutile d'échouer indéfiniment dessus
                if (offset == 1) {
                    throw IllegalStateException("Prometheus n'a renvoyé aucune mesure de puissance pour $period")
                }
                Log.warn("Pas de mesure de puissance pour $period (hors rétention ?), mois ignoré")
                continue
            }

            val hours = Duration.between(from, to).toMinutes() / 60.0
            val kwh = averageWatts * hours / 1000.0
            val costCents = BigDecimal.valueOf(kwh)
                .multiply(kwhPrice)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toInt()
            if (costCents <= 0) {
                Log.warn("Coût énergétique nul pour $period, aucune écriture créée")
                continue
            }

            val entry = FinanceEntryEntity().apply {
                id = UUID.randomUUID()
                type = EntryType.EXPENSE
                source = EntrySource.ENERGY
                label = "Électricité $period"
                amountCents = costCents
                entryDate = month.atEndOfMonth()
                this.period = period
                notes = "%.0f kWh estimés (puissance moyenne %.0f W)".format(kwh, averageWatts)
                createdAt = LocalDateTime.now()
            }
            if (financeEntries.saveIfAbsent(entry)) {
                created++
                Log.info("Dépense énergétique créée pour $period : ${entry.amountCents} cents")
            }
        }
        return created
    }
}
