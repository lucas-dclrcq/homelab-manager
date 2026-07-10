package org.hoohoot.homelab.manager.statistics.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord
import org.hoohoot.homelab.manager.statistics.domain.ports.PlaybackSessions
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

@ApplicationScoped
class PlaybackSessionRepository : PlaybackSessions {

    override suspend fun saveAll(records: List<PlaybackSessionRecord>) {
        if (records.isEmpty()) return
        val entities = records.map { it.toEntity() }
        Panache.withTransaction {
            PlaybackSessionEntity.persist(entities)
        }.awaitSuspending()
    }

    override suspend fun insertIgnoringDuplicates(records: List<PlaybackSessionRecord>): Int {
        if (records.isEmpty()) return 0
        return Panache.withTransaction {
            Panache.getSession().flatMap { session ->
                records.fold(Uni.createFrom().item(0)) { acc, record ->
                    acc.flatMap { inserted ->
                        session.createNativeQuery<Any>(INSERT_IGNORING_DUPLICATES)
                            .setParameter(1, UUID.randomUUID())
                            .setParameter(2, record.userId)
                            .setParameter(3, record.userName)
                            .setParameter(4, record.itemId)
                            .setParameter(5, record.itemName)
                            .setParameter(6, record.seriesId)
                            .setParameter(7, record.seriesName)
                            .setParameter(8, record.seasonNumber)
                            .setParameter(9, record.episodeNumber)
                            .setParameter(10, record.mediaType.name)
                            .setParameter(11, record.client)
                            .setParameter(12, record.deviceName)
                            .setParameter(13, record.platform)
                            .setParameter(14, record.startedAt)
                            .setParameter(15, record.endedAt)
                            .setParameter(16, record.playDurationSeconds)
                            .setParameter(17, record.runtimeSeconds)
                            .setParameter(18, record.progressPercent?.toBigDecimal(2))
                            .setParameter(19, record.completed)
                            .setParameter(20, record.source.name)
                            .setParameter(21, record.importKey)
                            .executeUpdate()
                            .map { count -> inserted + count }
                    }
                }
            }
        }.awaitSuspending()
    }

    private fun PlaybackSessionRecord.toEntity() = PlaybackSessionEntity().also {
        it.id = UUID.randomUUID()
        it.userId = userId
        it.userName = userName
        it.itemId = itemId
        it.itemName = itemName
        it.seriesId = seriesId
        it.seriesName = seriesName
        it.seasonNumber = seasonNumber
        it.episodeNumber = episodeNumber
        it.mediaType = mediaType
        it.client = client
        it.deviceName = deviceName
        it.platform = platform
        it.startedAt = startedAt
        it.endedAt = endedAt
        it.playDurationSeconds = playDurationSeconds
        it.runtimeSeconds = runtimeSeconds
        it.progressPercent = progressPercent?.toBigDecimal(2)
        it.completed = completed
        it.source = source
        it.importKey = importKey
        it.createdAt = LocalDateTime.now(ZoneOffset.UTC)
    }

    private fun Double.toBigDecimal(scale: Int): BigDecimal =
        BigDecimal.valueOf(this).setScale(scale, RoundingMode.HALF_UP)

    companion object {
        private val INSERT_IGNORING_DUPLICATES = """
            INSERT INTO playback_session (id, user_id, user_name, item_id, item_name, series_id, series_name,
                                          season_number, episode_number, media_type, client, device_name, platform,
                                          started_at, ended_at, play_duration_seconds, runtime_seconds,
                                          progress_percent, completed, source, import_key)
            VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13, ?14, ?15, ?16, ?17, ?18, ?19, ?20, ?21)
            ON CONFLICT (import_key) WHERE import_key IS NOT NULL DO NOTHING
        """.trimIndent()
    }
}
