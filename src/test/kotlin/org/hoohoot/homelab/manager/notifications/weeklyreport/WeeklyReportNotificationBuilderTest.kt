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

        assertThat(notification.body).contains("📰 Weekly Recap")
        // Releases grouped by day
        assertThat(notification.body).contains("Monday 17")
        assertThat(notification.body).contains("• Dune: Part Two (2024)")
        assertThat(notification.body).contains("• The Bear S03E01 \"Tomorrow\"")
        assertThat(notification.body).contains("Wednesday 19")
        assertThat(notification.body).contains("• Oppenheimer (2024)")
        // Stats
        assertThat(notification.body).contains("🏆 Top 3 Movies This Week")
        assertThat(notification.body).contains("🥇 Dune: Part Two — 5 viewers")
        assertThat(notification.body).contains("🥈 Oppenheimer — 3 viewers")
        assertThat(notification.body).contains("🥉 Poor Things — 2 viewers")
        assertThat(notification.body).contains("🏆 Top 3 Series This Week")
        assertThat(notification.body).contains("🥇 The Bear — 8 viewers")
        assertThat(notification.body).contains("🥈 Severance — 6 viewers")

        assertThat(notification.formattedBody).contains("<h2>📰 Weekly Recap</h2>")
        assertThat(notification.formattedBody).contains("<hr>")
        assertThat(notification.formattedBody).contains("<b>Monday 17</b>")
        assertThat(notification.formattedBody).contains("<b>Wednesday 19</b>")
        assertThat(notification.formattedBody).contains("<b>🏆 Top 3 Movies This Week</b>")
        assertThat(notification.formattedBody).contains("<b>🏆 Top 3 Series This Week</b>")
    }

    @Test
    fun `should group movies and episodes on same day together`() {
        val movies = listOf(
            RadarrMovie(title = "Some Movie", year = 2024, digitalRelease = "2025-03-18"),
        )
        val episodes = listOf(
            Episode(
                seasonNumber = 1, episodeNumber = 5, title = "Test",
                airDate = "2025-03-18", series = Series(title = "Test Show")
            ),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, episodes, emptyList(), emptyList()
        ).build()

        val body = notification.body
        val tuesdayIndex = body.indexOf("Tuesday 18")
        val movieIndex = body.indexOf("• Some Movie (2024)")
        val episodeIndex = body.indexOf("• Test Show S01E05 \"Test\"")

        assertThat(tuesdayIndex).isGreaterThan(-1)
        assertThat(movieIndex).isGreaterThan(tuesdayIndex)
        assertThat(episodeIndex).isGreaterThan(tuesdayIndex)
    }

    @Test
    fun `should hide releases section when empty`() {
        val topMovies = listOf(MostPopularMedia("Movie", 3))

        val notification = WeeklyReportNotificationBuilder(
            emptyList(), emptyList(), topMovies, emptyList()
        ).build()

        assertThat(notification.body).doesNotContain("Monday")
        assertThat(notification.body).doesNotContain("Tuesday")
        assertThat(notification.body).contains("🏆 Top 3 Movies This Week")
    }

    @Test
    fun `should hide stats section when no top movies or series`() {
        val notification = WeeklyReportNotificationBuilder(
            emptyList(), emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.body).contains("📰 Weekly Recap")
        assertThat(notification.body).doesNotContain("🏆")
        assertThat(notification.body).doesNotContain("━━━━━━━━━━━━━━━━━━━━")
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

        assertThat(notification.body).contains("🥇 A — 10 viewers")
        assertThat(notification.body).contains("🥈 B — 8 viewers")
        assertThat(notification.body).contains("🥉 C — 5 viewers")
        assertThat(notification.body).doesNotContain("D — 2 viewers")
    }

    @Test
    fun `should handle movie with only inCinemas date`() {
        val movies = listOf(
            RadarrMovie(title = "Cinema Movie", year = 2024, inCinemas = "2025-03-20"),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.body).contains("Thursday 20")
        assertThat(notification.body).contains("• Cinema Movie (2024)")
    }

    @Test
    fun `should show TBD group when no date available`() {
        val movies = listOf(
            RadarrMovie(title = "No Date Movie", year = 2024),
        )

        val notification = WeeklyReportNotificationBuilder(
            movies, emptyList(), emptyList(), emptyList()
        ).build()

        assertThat(notification.body).contains("TBD")
        assertThat(notification.body).contains("• No Date Movie (2024)")
    }
}
