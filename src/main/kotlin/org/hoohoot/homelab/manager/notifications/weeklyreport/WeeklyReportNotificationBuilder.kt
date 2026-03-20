package org.hoohoot.homelab.manager.notifications.weeklyreport

import kotlinx.datetime.LocalDate
import kotlinx.datetime.DayOfWeek
import org.hoohoot.homelab.manager.media.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.Notification
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.notifications.arr.sonarr.Episode

class WeeklyReportNotificationBuilder(
    private val movies: List<RadarrMovie>,
    private val episodes: List<Episode>,
    private val topMovies: List<MostPopularMedia>,
    private val topSeries: List<MostPopularMedia>,
) {
    fun build(): Notification {
        val textLines = mutableListOf<String>()
        val htmlLines = mutableListOf<String>()

        textLines.add("📰 Weekly Recap")
        htmlLines.add("<h2>📰 Weekly Recap</h2>")

        val hasReleases = movies.isNotEmpty() || episodes.isNotEmpty()
        val hasStats = topMovies.isNotEmpty() || topSeries.isNotEmpty()

        if (hasReleases) {
            textLines.add("")
            textLines.add("━━━━━━━━━━━━━━━━━━━━")
            htmlLines.add("<hr>")

            if (movies.isNotEmpty()) {
                textLines.add("")
                textLines.add("🎬 Movie Releases")
                htmlLines.add("<b>🎬 Movie Releases</b><br>")
                movies.forEach { movie ->
                    val date = formatMovieDate(movie)
                    val line = "• $date — ${movie.title ?: "Unknown"} (${movie.year ?: "?"})"
                    textLines.add(line)
                    htmlLines.add("$line<br>")
                }
            }

            if (episodes.isNotEmpty()) {
                textLines.add("")
                textLines.add("📺 TV Releases")
                htmlLines.add("<b>📺 TV Releases</b><br>")
                episodes.forEach { episode ->
                    val date = formatEpisodeDate(episode)
                    val seriesTitle = episode.series?.title ?: "Unknown"
                    val epCode = "S%02dE%02d".format(episode.seasonNumber, episode.episodeNumber)
                    val epTitle = episode.title?.let { "\"$it\"" } ?: ""
                    val line = "• $date — $seriesTitle $epCode $epTitle"
                    textLines.add(line)
                    htmlLines.add("$line<br>")
                }
            }
        }

        if (hasStats) {
            textLines.add("")
            textLines.add("━━━━━━━━━━━━━━━━━━━━")
            htmlLines.add("<hr>")

            if (topMovies.isNotEmpty()) {
                textLines.add("")
                textLines.add("🏆 Top 3 Movies This Week")
                htmlLines.add("<b>🏆 Top 3 Movies This Week</b><br>")
                topMovies.take(3).forEachIndexed { index, movie ->
                    val medal = medals[index]
                    val line = "$medal ${movie.name} — ${movie.uniqueViewers} viewers"
                    textLines.add(line)
                    htmlLines.add("$line<br>")
                }
            }

            if (topSeries.isNotEmpty()) {
                textLines.add("")
                textLines.add("🏆 Top 3 Series This Week")
                htmlLines.add("<b>🏆 Top 3 Series This Week</b><br>")
                topSeries.take(3).forEachIndexed { index, series ->
                    val medal = medals[index]
                    val line = "$medal ${series.name} — ${series.uniqueViewers} viewers"
                    textLines.add(line)
                    htmlLines.add("$line<br>")
                }
            }
        }

        return Notification(
            textMessage = textLines.joinToString("\n"),
            htmlMessage = htmlLines.joinToString("")
        )
    }

    private fun formatMovieDate(movie: RadarrMovie): String {
        val dateStr = movie.digitalRelease ?: movie.physicalRelease ?: movie.inCinemas
        return formatDateString(dateStr)
    }

    private fun formatEpisodeDate(episode: Episode): String {
        return formatDateString(episode.airDate)
    }

    private fun formatDateString(dateStr: String?): String {
        if (dateStr == null) return "TBD"
        return try {
            val date = LocalDate.parse(dateStr.substring(0, 10))
            val dayAbbr = date.dayOfWeek.toAbbreviation()
            val dayNum = date.dayOfMonth
            "$dayAbbr $dayNum"
        } catch (_: Exception) {
            "TBD"
        }
    }

    companion object {
        private val medals = listOf("🥇", "🥈", "🥉")

        private fun DayOfWeek.toAbbreviation(): String = when (this) {
            DayOfWeek.MONDAY -> "Mon"
            DayOfWeek.TUESDAY -> "Tue"
            DayOfWeek.WEDNESDAY -> "Wed"
            DayOfWeek.THURSDAY -> "Thu"
            DayOfWeek.FRIDAY -> "Fri"
            DayOfWeek.SATURDAY -> "Sat"
            DayOfWeek.SUNDAY -> "Sun"
            else -> "???"
        }
    }
}
