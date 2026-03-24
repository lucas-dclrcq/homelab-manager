package org.hoohoot.homelab.manager.notifications.weeklyreport

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.hoohoot.homelab.manager.media.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.arr.lidarr.LidarrAlbum
import org.hoohoot.homelab.manager.notifications.arr.radarr.RadarrMovie
import org.hoohoot.homelab.manager.notifications.arr.sonarr.Episode

class WeeklyReportNotificationBuilder(
    private val movies: List<RadarrMovie>,
    private val episodes: List<Episode>,
    private val albums: List<LidarrAlbum>,
    private val topMovies: List<MostPopularMedia>,
    private val topSeries: List<MostPopularMedia>,
) {
    private data class Release(val date: LocalDate?, val line: String)

    fun build(): RoomMessageEventContent.TextBased.Text {
        val textLines = mutableListOf<String>()
        val htmlLines = mutableListOf<String>()

        textLines.add("📰 Weekly Recap")
        htmlLines.add("<h2>📰 Weekly Recap</h2>")

        val hasReleases = movies.isNotEmpty() || episodes.isNotEmpty() || albums.isNotEmpty()
        val hasStats = topMovies.isNotEmpty() || topSeries.isNotEmpty()

        if (hasReleases) {
            textLines.add("")
            textLines.add("━━━━━━━━━━━━━━━━━━━━")
            htmlLines.add("<hr>")

            val releases = buildReleaseList()
            val grouped = releases.groupBy { it.date }
            val dated = grouped.filterKeys { it != null }.toSortedMap(compareBy { it })
            val undated = grouped[null]

            dated.forEach { (date, items) ->
                val header = "${date!!.dayOfWeek.toFullName()} ${date.dayOfMonth}"
                textLines.add("")
                textLines.add(header)
                htmlLines.add("<br><b>$header</b><br>")
                items.forEach { release ->
                    textLines.add(release.line)
                    htmlLines.add("${release.line}<br>")
                }
            }

            if (undated != null) {
                textLines.add("")
                textLines.add("TBD")
                htmlLines.add("<br><b>TBD</b><br>")
                undated.forEach { release ->
                    textLines.add(release.line)
                    htmlLines.add("${release.line}<br>")
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
                htmlLines.add("<br><b>🏆 Top 3 Movies This Week</b><br>")
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
                htmlLines.add("<br><b>🏆 Top 3 Series This Week</b><br>")
                topSeries.take(3).forEachIndexed { index, series ->
                    val medal = medals[index]
                    val line = "$medal ${series.name} — ${series.uniqueViewers} viewers"
                    textLines.add(line)
                    htmlLines.add("$line<br>")
                }
            }
        }

        return RoomMessageEventContent.TextBased.Text(
            body = textLines.joinToString("\n"),
            format = "org.matrix.custom.html",
            formattedBody = htmlLines.joinToString("")
        )
    }

    private fun buildReleaseList(): List<Release> {
        val releases = mutableListOf<Release>()

        movies.forEach { movie ->
            val dateTimeStr = movie.digitalRelease ?: movie.physicalRelease ?: movie.inCinemas
            val date = parseDate(dateTimeStr)
            val time = parseTime(dateTimeStr)
            val timeSuffix = if (time != null) " — $time" else ""
            val line = "🎬 ${movie.title ?: "Unknown"} (${movie.year ?: "?"})$timeSuffix"
            releases.add(Release(date, line))
        }

        episodes.forEach { episode ->
            val date = parseDate(episode.airDate)
            val time = parseTime(episode.airDateUtc)
            val seriesTitle = episode.series?.title ?: "Unknown"
            val epCode = "S%02dE%02d".format(episode.seasonNumber, episode.episodeNumber)
            val epTitle = episode.title?.let { " \"$it\"" } ?: ""
            val timeSuffix = if (time != null) " — $time" else ""
            val line = "📺 $seriesTitle $epCode$epTitle$timeSuffix"
            releases.add(Release(date, line))
        }

        albums.forEach { album ->
            val date = parseDate(album.releaseDate)
            val artistName = album.artist?.artistName ?: "Unknown"
            val albumTitle = album.title ?: "Unknown"
            val line = "🎵 $artistName — $albumTitle"
            releases.add(Release(date, line))
        }

        return releases
    }

    private fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr == null) return null
        return try {
            LocalDate.parse(dateStr.substring(0, 10))
        } catch (_: Exception) {
            null
        }
    }

    private fun parseTime(dateTimeStr: String?): String? {
        if (dateTimeStr == null) return null
        return try {
            val instant = Instant.parse(dateTimeStr)
            val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            if (local.hour == 0 && local.minute == 0) null
            else "%02d:%02d".format(local.hour, local.minute)
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private val medals = listOf("🥇", "🥈", "🥉")

        private fun DayOfWeek.toFullName(): String = when (this) {
            DayOfWeek.MONDAY -> "Monday"
            DayOfWeek.TUESDAY -> "Tuesday"
            DayOfWeek.WEDNESDAY -> "Wednesday"
            DayOfWeek.THURSDAY -> "Thursday"
            DayOfWeek.FRIDAY -> "Friday"
            DayOfWeek.SATURDAY -> "Saturday"
            DayOfWeek.SUNDAY -> "Sunday"
            else -> "???"
        }
    }
}
