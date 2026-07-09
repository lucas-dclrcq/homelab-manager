package org.hoohoot.homelab.manager.finances.domain.ports

import org.hoohoot.homelab.manager.finances.infra.FinanceSettingsEntity
import java.math.BigDecimal

interface FinanceSettings {
    suspend fun get(): FinanceSettingsEntity
    suspend fun updateKwhPrice(price: BigDecimal?): FinanceSettingsEntity
}
