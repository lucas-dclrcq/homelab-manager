package org.hoohoot.homelab.manager.it.config

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.vertx.VertxContextSupport
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import org.hoohoot.homelab.manager.statistics.infra.PlaybackSessionEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

internal object PlaybackSessionSeed {

    fun insertSession(
        userName: String,
        itemName: String,
        mediaType: MediaType,
        userId: String = userName,
        itemId: String = itemName,
        seriesId: String? = null,
        seriesName: String? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        startedAt: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(2),
        durationSeconds: Int = 3600,
        completed: Boolean = false,
        platform: String? = null,
        client: String? = null,
    ) {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction {
                PlaybackSessionEntity().apply {
                    id = UUID.randomUUID()
                    this.userId = userId
                    this.userName = userName
                    this.itemId = itemId
                    this.itemName = itemName
                    this.seriesId = seriesId
                    this.seriesName = seriesName
                    this.seasonNumber = seasonNumber
                    this.episodeNumber = episodeNumber
                    this.mediaType = mediaType
                    this.client = client
                    this.platform = platform
                    this.startedAt = startedAt
                    this.endedAt = startedAt.plusSeconds(durationSeconds.toLong())
                    this.playDurationSeconds = durationSeconds
                    this.completed = completed
                    this.source = SessionSource.POLLING
                    this.createdAt = LocalDateTime.now(ZoneOffset.UTC)
                }.persist<PlaybackSessionEntity>()
            }
        }
    }

    fun deleteAll() {
        VertxContextSupport.subscribeAndAwait {
            Panache.withTransaction { PlaybackSessionEntity.deleteAll() }
        }
    }
}
