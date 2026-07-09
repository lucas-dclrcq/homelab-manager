package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "finance_settings")
class FinanceSettingsEntity : PanacheEntityBase {
    @Id
    var id: Int = SINGLETON_ID

    @Column(name = "kwh_price", precision = 8, scale = 5)
    var kwhPrice: BigDecimal? = null

    @Column(name = "updated_at", nullable = false)
    lateinit var updatedAt: LocalDateTime

    companion object : PanacheCompanionBase<FinanceSettingsEntity, Int> {
        const val SINGLETON_ID = 1
    }
}
