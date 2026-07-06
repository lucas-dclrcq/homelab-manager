package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "homelab_event")
class HomelabEventEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "event_type", nullable = false)
    lateinit var eventType: String

    @Column(name = "title", nullable = false)
    lateinit var title: String

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details")
    var details: Map<String, String>? = null

    @Column(name = "occurred_at", nullable = false)
    lateinit var occurredAt: LocalDateTime

    companion object : PanacheCompanionBase<HomelabEventEntity, Long>
}
