package org.hoohoot.homelab.manager.statistics.infra.polling

import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession
import org.hoohoot.homelab.manager.statistics.domain.PlaybackSessionRecord
import org.hoohoot.homelab.manager.statistics.domain.Platforms
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

data class ActiveSession(
    val userId: String,
    val userName: String,
    val deviceId: String,
    val itemId: String,
    val itemName: String,
    val mediaType: MediaType,
    val seriesId: String? = null,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val client: String? = null,
    val deviceName: String? = null,
    val isPaused: Boolean = false,
    val positionTicks: Long? = null,
    val runTimeTicks: Long? = null,
)

private data class TrackerKey(val userId: String, val deviceId: String, val itemId: String)

private class TrackedSession(
    val startedAt: Instant,
    var lastSeenAt: Instant,
    var pausedSeconds: Long,
    var last: ActiveSession,
)

class SessionTracker(
    private val minPlaySeconds: Long,
    private val staleAfterSeconds: Long,
    private val completedThreshold: Double,
) {
    private val sessions = ConcurrentHashMap<TrackerKey, TrackedSession>()

    fun onSnapshot(snapshot: List<ActiveSession>, now: Instant): List<PlaybackSessionRecord> {
        val finished = mutableListOf<PlaybackSessionRecord>()
        val seenKeys = mutableSetOf<TrackerKey>()

        for (session in snapshot) {
            val key = TrackerKey(session.userId, session.deviceId, session.itemId)
            seenKeys += key
            val tracked = sessions[key]
            when {
                tracked == null -> sessions[key] = TrackedSession(now, now, 0, session)
                isStale(tracked, now) -> {
                    finalize(tracked)?.let(finished::add)
                    sessions[key] = TrackedSession(now, now, 0, session)
                }
                else -> {
                    if (session.isPaused) {
                        tracked.pausedSeconds += Duration.between(tracked.lastSeenAt, now).seconds
                    }
                    tracked.lastSeenAt = now
                    tracked.last = session
                }
            }
        }

        val iterator = sessions.entries.iterator()
        while (iterator.hasNext()) {
            val (key, tracked) = iterator.next()
            if (key !in seenKeys) {
                iterator.remove()
                finalize(tracked)?.let(finished::add)
            }
        }

        return finished
    }

    fun activeSessions(): List<NowPlayingSession> =
        sessions.values.map { tracked ->
            val session = tracked.last
            NowPlayingSession(
                userName = session.userName,
                itemName = session.itemName,
                seriesName = session.seriesName,
                seasonNumber = session.seasonNumber,
                episodeNumber = session.episodeNumber,
                mediaType = session.mediaType,
                progressPercent = progressPercent(session),
                paused = session.isPaused,
                client = session.client,
                platform = Platforms.fromClient(session.client),
                startedAt = tracked.startedAt.toUtcLocalDateTime(),
            )
        }.sortedBy { it.startedAt }

    private fun isStale(tracked: TrackedSession, now: Instant): Boolean =
        Duration.between(tracked.lastSeenAt, now).seconds >= staleAfterSeconds

    private fun finalize(tracked: TrackedSession): PlaybackSessionRecord? {
        val session = tracked.last
        val playDurationSeconds = Duration.between(tracked.startedAt, tracked.lastSeenAt).seconds - tracked.pausedSeconds
        if (playDurationSeconds < minPlaySeconds) return null

        val progress = progressPercent(session)
        return PlaybackSessionRecord(
            userId = session.userId,
            userName = session.userName,
            itemId = session.itemId,
            itemName = session.itemName,
            seriesId = session.seriesId,
            seriesName = session.seriesName,
            seasonNumber = session.seasonNumber,
            episodeNumber = session.episodeNumber,
            mediaType = session.mediaType,
            client = session.client,
            deviceName = session.deviceName,
            platform = Platforms.fromClient(session.client),
            startedAt = tracked.startedAt.toUtcLocalDateTime(),
            endedAt = tracked.lastSeenAt.toUtcLocalDateTime(),
            playDurationSeconds = playDurationSeconds.toInt(),
            runtimeSeconds = session.runTimeTicks?.let { (it / TICKS_PER_SECOND).toInt() },
            progressPercent = progress,
            completed = progress != null && progress >= completedThreshold * 100,
            source = SessionSource.POLLING,
        )
    }

    private fun progressPercent(session: ActiveSession): Double? {
        val runTimeTicks = session.runTimeTicks ?: return null
        val positionTicks = session.positionTicks ?: return null
        if (runTimeTicks <= 0) return null
        return (positionTicks.toDouble() / runTimeTicks * 100).coerceIn(0.0, 100.0)
    }

    private fun Instant.toUtcLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(this, ZoneOffset.UTC)

    companion object {
        private const val TICKS_PER_SECOND = 10_000_000L
    }
}
