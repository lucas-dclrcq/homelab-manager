package org.hoohoot.homelab.manager.statistics.infra

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanionBase
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "playback_session")
class PlaybackSessionEntity : PanacheEntityBase {
    @Id
    var id: UUID? = null

    @Column(name = "user_id", nullable = false)
    lateinit var userId: String

    @Column(name = "user_name", nullable = false)
    lateinit var userName: String

    @Column(name = "item_id", nullable = false)
    lateinit var itemId: String

    @Column(name = "item_name", nullable = false)
    lateinit var itemName: String

    @Column(name = "series_id")
    var seriesId: String? = null

    @Column(name = "series_name")
    var seriesName: String? = null

    @Column(name = "season_number")
    var seasonNumber: Int? = null

    @Column(name = "episode_number")
    var episodeNumber: Int? = null

    @Column(name = "media_type", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var mediaType: MediaType

    @Column(name = "client")
    var client: String? = null

    @Column(name = "device_name")
    var deviceName: String? = null

    @Column(name = "platform")
    var platform: String? = null

    @Column(name = "started_at", nullable = false)
    lateinit var startedAt: LocalDateTime

    @Column(name = "ended_at", nullable = false)
    lateinit var endedAt: LocalDateTime

    @Column(name = "play_duration_seconds", nullable = false)
    var playDurationSeconds: Int = 0

    @Column(name = "runtime_seconds")
    var runtimeSeconds: Int? = null

    @Column(name = "progress_percent")
    var progressPercent: BigDecimal? = null

    @Column(name = "completed", nullable = false)
    var completed: Boolean = false

    @Column(name = "source", nullable = false)
    @Enumerated(EnumType.STRING)
    lateinit var source: SessionSource

    @Column(name = "import_key")
    var importKey: String? = null

    @Column(name = "created_at", nullable = false)
    lateinit var createdAt: LocalDateTime

    companion object : PanacheCompanionBase<PlaybackSessionEntity, UUID>
}
