package org.hoohoot.homelab.manager.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "series_notification_thread")
class SeriesNotificationThreadEntity : PanacheEntityBase {
    @Id
    @Column(name = "series_id")
    lateinit var seriesId: String

    @Column(name = "event_id", nullable = false)
    lateinit var eventId: String

    @Column(name = "last_notified_at", nullable = false)
    lateinit var lastNotifiedAt: LocalDateTime

    companion object : PanacheCompanionBase<SeriesNotificationThreadEntity, String>

    constructor()

    constructor(seriesId: String, eventId: String, lastNotifiedAt: LocalDateTime) {
        this.seriesId = seriesId
        this.eventId = eventId
        this.lastNotifiedAt = lastNotifiedAt
    }
}
