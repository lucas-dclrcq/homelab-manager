package org.hoohoot.homelab.manager.notifications.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "media_notification_thread")
class MediaNotificationThreadEntity : PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "media_id", nullable = false)
    lateinit var mediaId: String

    @Column(name = "media_type", nullable = false)
    lateinit var mediaType: String

    @Column(name = "media_key")
    var mediaKey: String? = null

    @Column(name = "event_id", nullable = false)
    lateinit var eventId: String

    @Column(name = "last_notified_at", nullable = false)
    lateinit var lastNotifiedAt: LocalDateTime

    companion object : PanacheCompanionBase<MediaNotificationThreadEntity, Long>
}
