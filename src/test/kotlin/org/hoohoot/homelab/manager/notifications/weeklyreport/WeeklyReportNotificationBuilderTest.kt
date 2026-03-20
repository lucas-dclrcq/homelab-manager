package org.hoohoot.homelab.manager.notifications.weeklyreport

import org.assertj.core.api.Assertions.assertThat
import org.hoohoot.homelab.manager.media.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.notifications.arr.sonarr.Episode
import org.hoohoot.homelab.manager.notifications.arr.sonarr.Series
import org.junit.jupiter.api.Test

class WeeklyReportNotificationBuilderTest {

    @Test
    fun `should build notification with all sections`() {
        val movies = listOf(
            RadarrMovie(title = "Dune: Part Two", year = 2024, digitalRelease = "2025-03-17"),
            RadarrMovie(title = "Oppenheimer", year = 2024, physicalRelease = "2025-03-19"),
        )
        val episodes = listOf(
            Episode(
                seasonNumber = 3, episodeNumber = 1, title = "Tomorrow",
                airDate = "2025-03-17", series = Series(title = "The Bear")
            ),
        )
        val topMovies = listOf(
            MostPopularMedia("Dune: Part Two", 5),
            MostPopularMedia("Oppenheimer", 3),
            MostPopularMedia("Poor Things", 2),
        )
        val topSeries = listOf(
            MostPopularMedia("The Bear", 8),
            MostPopularMedia("Severance", 6),
        )

        val notification = WeeklyReportNotificationBuilder(movies, episodes, topMovies, topSeries).build()

        assertThat(notification.textMessage).contains("📰 Weekly Recap")
        assertThat(notification.textMessage).contains("🎬 Movie Releases")
        assertThat(notification.textMessage).contains("• Mon 17 — Dune: Part Two (2024)")
        assertThat(notification.textMessage).contains("• Wed 19 — Oppenheimer (2024)")
        assertThat(notification.textMessage).contains("📺 TV Releases")
        assertThat(notification.textMessage).contains("• Mon 17 — The Bear S03E01 \"Tomorrow\"")
        assertThat(notification.textMessage).contains("🏆 Top 3 Movies This Week")
        assertThat(notification.textMessage).contains("🥇 Dune: Part Two — 5 viewers")
        assertThat(notification.textMessage).contains("🥈 Oppenheimer — 3 viewers")
        assertThat(notification.textMessage).contains("🥉 Poor Things — 2 viewers")
        assertThat(notification.textMessage).contains("🏆 Top 3 Series This Week")
        assertThat(notification.textMessage).contains("🥇 The Bear — 8 viewers")
        assertThat(notification.textMessage).contains("🥈 Severance — 6 viewers")

        assertThat(notification.htmlMessage).contains("<h2>📰 Weekly Recap</h2>")
        assertThat(notification.htmlMessage).contains("<hr>")
        assertThat(notification.htmlMessage).contains("<b>🎬 Movie Releases</b>")
        assertThat(notification.htmlMessage).contains("<b>📺 TV Releases</b>")
        assertThat(notification.htmlMessage).contains("<b>🏆 Top 3 Movies This Week</b>")
        assertThat(notification.htmlMessage).contains("<b>🏆 Top 3 Series This Week</b>")
    }

    @Test
    fun `should hide movie releases section when empty`() {
        val episodes = listOf(
            Episode(
                seasonNumber = 1, episodeNumber = 5, title = "Test",
                airDate = "2025-03-18", series = Series(title = "Test Show")
            ),
        )
        val topMovies = listOf(MostPopularMedia("Movie", 3))

        val notification = WeeklyReportNotificationBuilder(
            emptyList(), episodes, topMovies, emptyList()
        ).build()

        assertThat(notification.textMessage).doesNotContain("🎬 Movie Releases")
        assertThat(notification.textMessage).contains("📺 TV Releases")
    }

    @Test
    fun `should hide tv releases section when empty`() {
        val movies = listOf(
            RadarrMovie(title = "Test Movie", year = 2024, digitalRelease = "2025-03-17"),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.textMessage).contains("🎬 Movie Releases")
        assertThat(notification.textMessage).doesNotContain("📺 TV Releases")
    }

    @Test
    fun `should hide stats section when no top movies or series`() {
        val notification = WeeklyReportNotificationBuilder(
            emptyList(), emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.textMessage).contains("📰 Weekly Recap")
        assertThat(notification.textMessage).doesNotContain("🏆")
        assertThat(notification.textMessage).doesNotContain("━━━━━━━━━━━━━━━━━━━━")
    }

    @Test
    fun `should limit top lists to 3 entries`() {
        val topMovies = listOf(
            MostPopularMedia("A", 10),
            MostPopularMedia("B", 8),
            MostPopularMedia("C", 5),
            MostPopularMedia("D", 2),
        )

        val notification = WeeklyReportNotificationBuilder(
            emptyList(), emptyList(), topMovies, emptyList()
        ).build()

        assertThat(notification.textMessage).contains("🥇 A — 10 viewers")
        assertThat(notification.textMessage).contains("🥈 B — 8 viewers")
        assertThat(notification.textMessage).contains("🥉 C — 5 viewers")
        assertThat(notification.textMessage).doesNotContain("D — 2 viewers")
    }

    @Test
    fun `should handle movie with only inCinemas date`() {
        val movies = listOf(
            RadarrMovie(title = "Cinema Movie", year = 2024, inCinemas = "2025-03-20"),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.textMessage).contains("• Thu 20 — Cinema Movie (2024)")
    }

    @Test
    fun `should show TBD when no date available`() {
        val movies = listOf(
            RadarrMovie(title = "No Date Movie", year = 2024),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.textMessage).contains("• TBD — No Date Movie (2024)")
    }
}
