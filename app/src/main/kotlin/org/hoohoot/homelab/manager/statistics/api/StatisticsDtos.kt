package org.hoohoot.homelab.manager.statistics.api

import org.hoohoot.homelab.manager.statistics.domain.HourActivity
import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession
import org.hoohoot.homelab.manager.statistics.domain.PlatformShare
import org.hoohoot.homelab.manager.statistics.domain.StatisticsSummary
import org.hoohoot.homelab.manager.statistics.domain.TimeGranularity
import org.hoohoot.homelab.manager.statistics.domain.TimePoint
import org.hoohoot.homelab.manager.statistics.domain.TopUser
import org.hoohoot.homelab.manager.statistics.domain.WeekdayActivity
import org.hoohoot.homelab.manager.statistics.domain.usecases.PlaysOverTime
import org.hoohoot.homelab.manager.statistics.domain.usecases.TopMediaItem
import java.time.LocalDateTime

data class StatisticsSummaryDto(
    val totalWatchTimeSeconds: Long,
    val playCount: Long,
    val completedItems: Long,
    val peakHour: Int?,
)

data class TopUserDto(
    val userName: String,
    val watchTimeSeconds: Long,
    val itemsWatched: Long,
    val playCount: Long,
)

data class TopMediaDto(
    val name: String,
    val plays: Long,
    val watchTimeSeconds: Long,
    val uniqueViewers: Long,
    val completionRate: Double?,
    val bingeScore: Int?,
)

data class WeekdayActivityDto(val isoDayOfWeek: Int, val plays: Long, val watchTimeSeconds: Long)

data class HourActivityDto(val hour: Int, val plays: Long, val watchTimeSeconds: Long)

data class PlatformShareDto(val platform: String, val plays: Long, val watchTimeSeconds: Long)

data class TimePointDto(val bucketStart: LocalDateTime, val plays: Long, val watchTimeSeconds: Long)

data class PlaysOverTimeDto(val granularity: TimeGranularity, val points: List<TimePointDto>)

data class NowPlayingDto(
    val userName: String,
    val itemName: String,
    val seriesName: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val mediaType: String,
    val progressPercent: Double?,
    val paused: Boolean,
    val client: String?,
    val platform: String,
    val startedAt: LocalDateTime,
)

fun StatisticsSummary.toDto() = StatisticsSummaryDto(
    totalWatchTimeSeconds = totalWatchTimeSeconds,
    playCount = playCount,
    completedItems = completedItems,
    peakHour = peakHour,
)

fun TopUser.toDto() = TopUserDto(
    userName = userName,
    watchTimeSeconds = watchTimeSeconds,
    itemsWatched = itemsWatched,
    playCount = playCount,
)

fun TopMediaItem.toDto() = TopMediaDto(
    name = name,
    plays = plays,
    watchTimeSeconds = watchTimeSeconds,
    uniqueViewers = uniqueViewers,
    completionRate = completionRate,
    bingeScore = bingeScore,
)

fun WeekdayActivity.toDto() = WeekdayActivityDto(isoDayOfWeek, plays, watchTimeSeconds)

fun HourActivity.toDto() = HourActivityDto(hour, plays, watchTimeSeconds)

fun PlatformShare.toDto() = PlatformShareDto(platform, plays, watchTimeSeconds)

fun TimePoint.toDto() = TimePointDto(bucketStart, plays, watchTimeSeconds)

fun PlaysOverTime.toDto() = PlaysOverTimeDto(granularity, points.map { it.toDto() })

fun NowPlayingSession.toDto() = NowPlayingDto(
    userName = userName,
    itemName = itemName,
    seriesName = seriesName,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    mediaType = mediaType.name,
    progressPercent = progressPercent,
    paused = paused,
    client = client,
    platform = platform,
    startedAt = startedAt,
)
