package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hoohoot.homelab.manager.finances.domain.EntryType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "finance_recurring_rule")
class RecurringRuleEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: EntryType

    @Column(name = "label", nullable = false)
    lateinit var label: String

    @Column(name = "amount_cents", nullable = false)
    var amountCents: Int = 0

    @Column(name = "day_of_month", nullable = false)
    var dayOfMonth: Int = 1

    @Column(name = "member_id")
    var memberId: UUID? = null

    @Column(name = "vendor")
    var vendor: String? = null

    @Column(name = "active", nullable = false)
    var active: Boolean = true

    @Column(name = "start_date", nullable = false)
    lateinit var startDate: LocalDate

    @Column(name = "end_date")
    var endDate: LocalDate? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    companion object : PanacheCompanionBase<RecurringRuleEntity, UUID>
}
