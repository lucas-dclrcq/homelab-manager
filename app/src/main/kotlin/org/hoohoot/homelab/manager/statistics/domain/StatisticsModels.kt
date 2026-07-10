package org.hoohoot.homelab.manager.statistics.domain

import java.time.LocalDateTime

enum class MediaType { MOVIE, EPISODE }

enum class SessionSource { POLLING, IMPORT }

enum class StatsPeriod { TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL_TIME }

enum class MediaKind { MOVIE, SERIES }

enum class TopMediaSort { NAME, PLAYS, WATCH_TIME, UNIQUE_VIEWERS, COMPLETION_RATE, BINGE_SCORE }

enum class SortDirection { ASC, DESC }

enum class TimeGranularity { HOUR, DAY, MONTH }

data class PlaybackSessionRecord(
    val userId: String,
    val userName: String,
    val itemId: String,
    val itemName: String,
    val seriesId: String? = null,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val mediaType: MediaType,
    val client: String? = null,
    val deviceName: String? = null,
    val platform: String? = null,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val playDurationSeconds: Int,
    val runtimeSeconds: Int? = null,
    val progressPercent: Double? = null,
    val completed: Boolean = false,
    val source: SessionSource,
    val importKey: String? = null,
)

data class StatisticsSummary(
    val totalWatchTimeSeconds: Long,
    val playCount: Long,
    val completedItems: Long,
    val peakHour: Int?,
)

data class TopUser(
    val userId: String,
    val userName: String,
    val watchTimeSeconds: Long,
    val itemsWatched: Long,
    val playCount: Long,
)

data class TopSeries(
    val seriesId: String,
    val name: String,
    val plays: Long,
    val watchTimeSeconds: Long,
    val uniqueViewers: Long,
    val completionRate: Double?,
    val bingeScore: Int,
)

data class TopMovie(
    val itemId: String,
    val name: String,
    val plays: Long,
    val watchTimeSeconds: Long,
    val uniqueViewers: Long,
    val completionRate: Double?,
)

data class WeekdayActivity(val isoDayOfWeek: Int, val plays: Long, val watchTimeSeconds: Long)

data class HourActivity(val hour: Int, val plays: Long, val watchTimeSeconds: Long)

data class PlatformShare(val platform: String, val plays: Long, val watchTimeSeconds: Long)

data class TimePoint(val bucketStart: LocalDateTime, val plays: Long, val watchTimeSeconds: Long)

data class MostPopular(val name: String, val uniqueViewers: Long)

data class MostViewed(val name: String, val plays: Long, val watchTimeSeconds: Long)

data class SeriesWatcher(
    val userName: String,
    val episodesWatched: Long,
    val lastEpisodeName: String,
    val lastSeasonNumber: Int?,
    val lastEpisodeNumber: Int?,
)

data class NowPlayingSession(
    val userName: String,
    val itemName: String,
    val seriesName: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val mediaType: MediaType,
    val progressPercent: Double?,
    val paused: Boolean,
    val client: String?,
    val platform: String,
    val startedAt: LocalDateTime,
)

object Platforms {
    const val OTHER = "OTHER"

    fun fromClient(client: String?): String {
        val normalized = client?.lowercase() ?: return OTHER
        return when {
            "android tv" in normalized -> "ANDROID_TV"
            "android" in normalized -> "ANDROID"
            "web" in normalized -> "WEB"
            "ios" in normalized -> "IOS"
            "chromecast" in normalized -> "CHROMECAST"
            "media player" in normalized -> "DESKTOP"
            "kodi" in normalized -> "KODI"
            else -> OTHER
        }
    }
}
