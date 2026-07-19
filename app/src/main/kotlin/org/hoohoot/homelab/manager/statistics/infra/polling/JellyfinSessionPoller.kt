package org.hoohoot.homelab.manager.statistics.infra.polling

import io.quarkus.logging.Log
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.context.control.ActivateRequestContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinRestClient
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinSessionDto
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession
import org.hoohoot.homelab.manager.statistics.domain.PlaybackMethod
import org.hoohoot.homelab.manager.statistics.domain.ports.NowPlayingSource
import org.hoohoot.homelab.manager.statistics.domain.ports.PlaybackSessions
import java.time.Duration
import java.time.Instant

/**
 * Polle GET /Sessions de Jellyfin à intervalle court pour alimenter l'historique de visionnage.
 *
 * Volontairement hors du pattern ManagedJob/JobRunner : un tick toutes les 15 s inonderait
 * la table job_execution. Les erreurs (Jellyfin injoignable) sont loguées en warn throttlé.
 */
@ApplicationScoped
class JellyfinSessionPoller(
    @param:RestClient private val jellyfin: JellyfinRestClient,
    private val playbackSessions: PlaybackSessions,
    @ConfigProperty(name = "statistics.min-play-seconds") minPlaySeconds: Long,
    @ConfigProperty(name = "statistics.stale-after-seconds") staleAfterSeconds: Long,
    @ConfigProperty(name = "statistics.completed-threshold") completedThreshold: Double,
) : NowPlayingSource {
    private val tracker = SessionTracker(minPlaySeconds, staleAfterSeconds, completedThreshold)
    private var lastErrorLogAt: Instant = Instant.EPOCH

    override fun current(): List<NowPlayingSession> = tracker.activeSessions()

    @Scheduled(
        identity = "statistics-session-poller",
        every = "{statistics.poll.every}",
        concurrentExecution = Scheduled.ConcurrentExecution.SKIP,
    )
    @ActivateRequestContext
    suspend fun poll() {
        val snapshot = try {
            jellyfin.getSessions()
        } catch (e: Exception) {
            val now = Instant.now()
            if (Duration.between(lastErrorLogAt, now) >= ERROR_LOG_INTERVAL) {
                lastErrorLogAt = now
                Log.warn("Polling des sessions Jellyfin impossible: ${e.message}")
            }
            return
        }

        val finished = tracker.onSnapshot(snapshot.mapNotNull { it.toActiveSession() }, Instant.now())
        if (finished.isNotEmpty()) {
            playbackSessions.saveAll(finished)
            Log.info("${finished.size} session(s) de visionnage enregistrée(s)")
        }
    }

    companion object {
        private val ERROR_LOG_INTERVAL: Duration = Duration.ofMinutes(5)

        fun JellyfinSessionDto.toActiveSession(): ActiveSession? {
            val item = nowPlayingItem ?: return null
            val mediaType = when (item.type) {
                "Movie" -> MediaType.MOVIE
                "Episode" -> MediaType.EPISODE
                // Trailers, audio, live TV, livres... hors périmètre des stats
                else -> return null
            }
            val videoStream = item.mediaStreams?.firstOrNull { it.type == "Video" }
            val audioStream = item.mediaStreams?.firstOrNull { it.type == "Audio" }
            return ActiveSession(
                userId = userId ?: return null,
                userName = userName ?: return null,
                deviceId = deviceId ?: return null,
                itemId = item.id ?: return null,
                itemName = item.name ?: return null,
                mediaType = mediaType,
                seriesId = item.seriesId,
                seriesName = item.seriesName,
                seasonNumber = item.parentIndexNumber,
                episodeNumber = item.indexNumber,
                client = client,
                deviceName = deviceName,
                isPaused = playState?.isPaused ?: false,
                positionTicks = playState?.positionTicks,
                runTimeTicks = item.runTimeTicks,
                playMethod = playState?.playMethod?.toPlaybackMethod(),
                videoCodec = videoStream?.codec,
                audioCodec = audioStream?.codec,
                videoHeight = videoStream?.height,
            )
        }

        // Jellyfin renvoie "DirectPlay" / "DirectStream" / "Transcode"
        private fun String.toPlaybackMethod(): PlaybackMethod =
            if (equals("Transcode", ignoreCase = true)) PlaybackMethod.TRANSCODE else PlaybackMethod.DIRECT
    }
}
