package org.hoohoot.homelab.manager.statistics

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinNowPlayingItem
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinPlayState
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinSessionDto
import org.hoohoot.homelab.manager.statistics.domain.MediaType
import org.hoohoot.homelab.manager.statistics.domain.SessionSource
import org.hoohoot.homelab.manager.statistics.infra.polling.ActiveSession
import org.hoohoot.homelab.manager.statistics.infra.polling.JellyfinSessionPoller.Companion.toActiveSession
import org.hoohoot.homelab.manager.statistics.infra.polling.SessionTracker
import org.junit.jupiter.api.Test
import java.time.Instant

internal class SessionTrackerTest {

    private val tracker = SessionTracker(minPlaySeconds = 120, staleAfterSeconds = 600, completedThreshold = 0.85)
    private val t0: Instant = Instant.parse("2026-07-01T20:00:00Z")

    private fun session(
        userId: String = "user-1",
        userName: String = "alice",
        deviceId: String = "device-1",
        itemId: String = "item-1",
        itemName: String = "Un film",
        mediaType: MediaType = MediaType.MOVIE,
        isPaused: Boolean = false,
        positionTicks: Long? = null,
        runTimeTicks: Long? = 36_000_000_000L,
    ) = ActiveSession(
        userId = userId,
        userName = userName,
        deviceId = deviceId,
        itemId = itemId,
        itemName = itemName,
        mediaType = mediaType,
        client = "Jellyfin Web",
        isPaused = isPaused,
        positionTicks = positionTicks,
        runTimeTicks = runTimeTicks,
    )

    @Test
    fun `une session disparue est finalisee avec sa duree de visionnage`() {
        tracker.onSnapshot(listOf(session()), t0)
        tracker.onSnapshot(listOf(session()), t0.plusSeconds(300))
        val finished = tracker.onSnapshot(emptyList(), t0.plusSeconds(315))

        assertThat(finished).hasSize(1)
        val record = finished.single()
        assertThat(record.playDurationSeconds).isEqualTo(300)
        assertThat(record.userName).isEqualTo("alice")
        assertThat(record.source).isEqualTo(SessionSource.POLLING)
        assertThat(record.platform).isEqualTo("WEB")
        assertThat(record.startedAt).isEqualTo("2026-07-01T20:00:00")
        assertThat(record.endedAt).isEqualTo("2026-07-01T20:05:00")
    }

    @Test
    fun `une session trop courte est jetee`() {
        tracker.onSnapshot(listOf(session()), t0)
        tracker.onSnapshot(listOf(session()), t0.plusSeconds(60))
        val finished = tracker.onSnapshot(emptyList(), t0.plusSeconds(75))

        assertThat(finished).isEmpty()
    }

    @Test
    fun `le temps de pause est deduit de la duree visionnee`() {
        tracker.onSnapshot(listOf(session()), t0)
        tracker.onSnapshot(listOf(session()), t0.plusSeconds(120))
        tracker.onSnapshot(listOf(session(isPaused = true)), t0.plusSeconds(180))
        tracker.onSnapshot(listOf(session(isPaused = true)), t0.plusSeconds(240))
        tracker.onSnapshot(listOf(session()), t0.plusSeconds(300))
        val finished = tracker.onSnapshot(emptyList(), t0.plusSeconds(315))

        assertThat(finished.single().playDurationSeconds).isEqualTo(300 - 120)
    }

    @Test
    fun `la progression et la completion sont calculees depuis les ticks`() {
        val almostDone = session(positionTicks = 33_000_000_000L)
        tracker.onSnapshot(listOf(almostDone), t0)
        tracker.onSnapshot(listOf(almostDone), t0.plusSeconds(300))
        val finished = tracker.onSnapshot(emptyList(), t0.plusSeconds(315))

        val record = finished.single()
        assertThat(record.progressPercent).isCloseTo(91.7, org.assertj.core.api.Assertions.within(0.1))
        assertThat(record.completed).isTrue()
        assertThat(record.runtimeSeconds).isEqualTo(3600)
    }

    @Test
    fun `une progression sous le seuil n'est pas completee`() {
        val halfway = session(positionTicks = 18_000_000_000L)
        tracker.onSnapshot(listOf(halfway), t0)
        tracker.onSnapshot(listOf(halfway), t0.plusSeconds(300))
        val finished = tracker.onSnapshot(emptyList(), t0.plusSeconds(315))

        assertThat(finished.single().completed).isFalse()
    }

    @Test
    fun `un trou de polling superieur au timeout cloture l'ancienne session et en demarre une nouvelle`() {
        tracker.onSnapshot(listOf(session()), t0)
        tracker.onSnapshot(listOf(session()), t0.plusSeconds(300))
        val finished = tracker.onSnapshot(listOf(session()), t0.plusSeconds(1500))

        assertThat(finished).hasSize(1)
        assertThat(finished.single().playDurationSeconds).isEqualTo(300)

        tracker.onSnapshot(listOf(session()), t0.plusSeconds(1800))
        val second = tracker.onSnapshot(emptyList(), t0.plusSeconds(1815))
        assertThat(second.single().playDurationSeconds).isEqualTo(300)
    }

    @Test
    fun `changer d'item sur le meme device produit deux sessions`() {
        tracker.onSnapshot(listOf(session(itemId = "movie-1", itemName = "Film 1")), t0)
        tracker.onSnapshot(listOf(session(itemId = "movie-1", itemName = "Film 1")), t0.plusSeconds(300))
        val first = tracker.onSnapshot(listOf(session(itemId = "movie-2", itemName = "Film 2")), t0.plusSeconds(315))

        assertThat(first).hasSize(1)
        assertThat(first.single().itemName).isEqualTo("Film 1")

        tracker.onSnapshot(listOf(session(itemId = "movie-2", itemName = "Film 2")), t0.plusSeconds(615))
        val second = tracker.onSnapshot(emptyList(), t0.plusSeconds(630))
        assertThat(second).hasSize(1)
        assertThat(second.single().itemName).isEqualTo("Film 2")
    }

    @Test
    fun `activeSessions reflete le dernier snapshot`() {
        tracker.onSnapshot(
            listOf(session(positionTicks = 9_000_000_000L, isPaused = true)),
            t0,
        )

        val active = tracker.activeSessions()
        assertThat(active).hasSize(1)
        val nowPlaying = active.single()
        assertThat(nowPlaying.userName).isEqualTo("alice")
        assertThat(nowPlaying.paused).isTrue()
        assertThat(nowPlaying.progressPercent).isCloseTo(25.0, org.assertj.core.api.Assertions.within(0.1))
        assertThat(nowPlaying.platform).isEqualTo("WEB")
    }

    @Test
    fun `le mapping Jellyfin ignore les sessions sans lecture ou hors films et episodes`() {
        val idle = JellyfinSessionDto(userId = "u", userName = "alice", deviceId = "d")
        val trailer = JellyfinSessionDto(
            userId = "u", userName = "alice", deviceId = "d",
            nowPlayingItem = JellyfinNowPlayingItem(id = "t", name = "Trailer", type = "Trailer"),
        )
        val movie = JellyfinSessionDto(
            userId = "u", userName = "alice", deviceId = "d",
            playState = JellyfinPlayState(isPaused = false, positionTicks = 0),
            nowPlayingItem = JellyfinNowPlayingItem(id = "m", name = "Film", type = "Movie", runTimeTicks = 1L),
        )
        val episode = JellyfinSessionDto(
            userId = "u", userName = "alice", deviceId = "d",
            nowPlayingItem = JellyfinNowPlayingItem(
                id = "e", name = "Pilot", type = "Episode",
                seriesId = "s", seriesName = "Show", parentIndexNumber = 1, indexNumber = 2,
            ),
        )

        assertThat(idle.toActiveSession()).isNull()
        assertThat(trailer.toActiveSession()).isNull()
        assertThat(movie.toActiveSession()?.mediaType).isEqualTo(MediaType.MOVIE)
        val mapped = episode.toActiveSession()
        assertThat(mapped?.mediaType).isEqualTo(MediaType.EPISODE)
        assertThat(mapped?.seriesId).isEqualTo("s")
        assertThat(mapped?.seasonNumber).isEqualTo(1)
        assertThat(mapped?.episodeNumber).isEqualTo(2)
    }
}
