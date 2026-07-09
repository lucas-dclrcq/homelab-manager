package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.FinanceSettings
import java.math.BigDecimal
import java.time.LocalDateTime

@ApplicationScoped
class FinanceSettingsRepository : FinanceSettings {

    override suspend fun get(): FinanceSettingsEntity =
        Panache.withSession {
            FinanceSettingsEntity.findById(FinanceSettingsEntity.SINGLETON_ID)
        }.awaitSuspending() ?: error("finance_settings singleton row is missing (seeded by V16 migration)")

    override suspend fun updateKwhPrice(price: BigDecimal?): FinanceSettingsEntity =
        Panache.withTransaction {
            FinanceSettingsEntity.findById(FinanceSettingsEntity.SINGLETON_ID).invoke { entity ->
                entity?.let {
                    it.kwhPrice = price
                    it.updatedAt = LocalDateTime.now()
                }
            }
        }.awaitSuspending() ?: error("finance_settings singleton row is missing (seeded by V16 migration)")
}
