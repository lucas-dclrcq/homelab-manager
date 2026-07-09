package org.hoohoot.homelab.manager.finances.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceSettings
import org.hoohoot.homelab.manager.finances.infra.FinanceSettingsEntity

@ApplicationScoped
class GetFinanceSettings(
    private val financeSettings: FinanceSettings,
) {
    suspend operator fun invoke(): FinanceSettingsEntity = financeSettings.get()
}
