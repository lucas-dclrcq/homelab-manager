package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceSettings
import org.hoohoot.homelab.manager.finances.infra.FinanceSettingsEntity
import java.math.BigDecimal

@ApplicationScoped
class UpdateFinanceSettings(
    private val financeSettings: FinanceSettings,
) {
    suspend operator fun invoke(kwhPrice: BigDecimal?): FinanceSettingsEntity =
        financeSettings.updateKwhPrice(kwhPrice)
}
