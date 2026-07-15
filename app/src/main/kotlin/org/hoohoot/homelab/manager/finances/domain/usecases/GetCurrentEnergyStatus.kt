package org.hoohoot.homelab.manager.finances.domain.usecases

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.EnergyMetrics
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceSettings
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant

private const val HOURS_PER_MONTH = 730.5

data class EnergyStatus(
    val currentPowerWatts: Double?,
    val estimatedMonthlyKwh: Double?,
    val estimatedMonthlyCostCents: Long?,
    val kwhPriceConfigured: Boolean,
)

@ApplicationScoped
class GetCurrentEnergyStatus(
    private val energyMetrics: EnergyMetrics,
    private val financeSettings: FinanceSettings,
) {
    suspend operator fun invoke(): EnergyStatus {
        val kwhPrice = financeSettings.get().kwhPrice

        val currentWatts = runCatching { energyMetrics.currentPowerWatts() }
            .onFailure { Log.warn("Impossible de récupérer la puissance instantanée depuis Prometheus", it) }
            .getOrNull()
        val now = Instant.now()
        val averageWatts = runCatching { energyMetrics.averagePowerWatts(now.minus(Duration.ofDays(30)), now) }
            .onFailure { Log.warn("Impossible de récupérer la puissance moyenne depuis Prometheus", it) }
            .getOrNull()

        val estimatedMonthlyKwh = averageWatts?.let { it * HOURS_PER_MONTH / 1000.0 }
        val estimatedMonthlyCostCents = if (estimatedMonthlyKwh != null && kwhPrice != null) {
            BigDecimal.valueOf(estimatedMonthlyKwh)
                .multiply(kwhPrice)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .toLong()
        } else null

        return EnergyStatus(
            currentPowerWatts = currentWatts,
            estimatedMonthlyKwh = estimatedMonthlyKwh,
            estimatedMonthlyCostCents = estimatedMonthlyCostCents,
            kwhPriceConfigured = kwhPrice != null,
        )
    }
}
