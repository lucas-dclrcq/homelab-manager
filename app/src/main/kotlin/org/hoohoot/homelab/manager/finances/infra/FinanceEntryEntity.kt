package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hoohoot.homelab.manager.finances.domain.EntrySource
import org.hoohoot.homelab.manager.finances.domain.EntryType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "finance_entry")
class FinanceEntryEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var type: EntryType

    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var source: EntrySource

    @Column(name = "label", nullable = false)
    lateinit var label: String

    @Column(name = "vendor")
    var vendor: String? = null

    @Column(name = "amount_cents", nullable = false)
    var amountCents: Int = 0

    @Column(name = "entry_date", nullable = false)
    lateinit var entryDate: LocalDate

    @Column(name = "member_id")
    var memberId: UUID? = null

    @Column(name = "rule_id")
    var ruleId: UUID? = null

    @Column(name = "period")
    var period: String? = null

    @Column(name = "notes")
    var notes: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    companion object : PanacheCompanionBase<FinanceEntryEntity, UUID>
}
