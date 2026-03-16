package org.hoohoot.homelab.manager.notifications.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "notification_sent")
class NotificationSentEntity : PanacheEntityBase {
    @Id
    @Column(name = "issue_id")
    lateinit var issueId: String

    @Column(name = "event_id", nullable = false)
    lateinit var eventId: String

    companion object : PanacheCompanionBase<NotificationSentEntity, String>

    constructor()

    constructor(issueId: String, eventId: String) {
        this.issueId = issueId
        this.eventId = eventId
    }
}
