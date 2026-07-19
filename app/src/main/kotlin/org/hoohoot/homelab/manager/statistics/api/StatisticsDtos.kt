package org.hoohoot.homelab.manager.statistics.api

import org.hoohoot.homelab.manager.statistics.domain.HeatmapCell
import org.hoohoot.homelab.manager.statistics.domain.HourActivity
import org.hoohoot.homelab.manager.statistics.domain.NowPlayingSession
import org.hoohoot.homelab.manager.statistics.domain.PlatformShare
import org.hoohoot.homelab.manager.statistics.domain.QualityBreakdown
import org.hoohoot.homelab.manager.statistics.domain.QualitySlice
import org.hoohoot.homelab.manager.statistics.domain.SessionHistoryEntry
import org.hoohoot.homelab.manager.statistics.domain.SessionHistoryPage
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
    val activeUsers: Long,
)

data class TopUserDto(
    val userName: String,
    val watchTimeSeconds: Long,
    val itemsWatched: Long,
    val playCount: Long,
    val lastSeen: LocalDateTime?,
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

data class HeatmapCellDto(val isoDayOfWeek: Int, val hour: Int, val plays: Long)

data class QualitySliceDto(val label: String, val plays: Long, val watchTimeSeconds: Long)

data class QualityBreakdownDto(
    val resolutions: List<QualitySliceDto>,
    val videoCodecs: List<QualitySliceDto>,
    val audioCodecs: List<QualitySliceDto>,
    val playbackMethods: List<QualitySliceDto>,
)

data class SessionHistoryEntryDto(
    val userName: String,
    val itemName: String,
    val seriesName: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val mediaType: String,
    val startedAt: LocalDateTime,
    val playDurationSeconds: Long,
    val progressPercent: Double?,
    val completed: Boolean,
    val client: String?,
    val deviceName: String?,
    val platform: String,
    val playMethod: String?,
    val videoCodec: String?,
    val audioCodec: String?,
    val resolution: String?,
)

data class SessionHistoryPageDto(
    val items: List<SessionHistoryEntryDto>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalCount: Long,
)

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
    activeUsers = activeUsers,
)

fun TopUser.toDto() = TopUserDto(
    userName = userName,
    watchTimeSeconds = watchTimeSeconds,
    itemsWatched = itemsWatched,
    playCount = playCount,
    lastSeen = lastSeen,
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

fun HeatmapCell.toDto() = HeatmapCellDto(isoDayOfWeek, hour, plays)

fun QualitySlice.toDto() = QualitySliceDto(label, plays, watchTimeSeconds)

fun QualityBreakdown.toDto() = QualityBreakdownDto(
    resolutions = resolutions.map { it.toDto() },
    videoCodecs = videoCodecs.map { it.toDto() },
    audioCodecs = audioCodecs.map { it.toDto() },
    playbackMethods = playbackMethods.map { it.toDto() },
)

fun SessionHistoryEntry.toDto() = SessionHistoryEntryDto(
    userName = userName,
    itemName = itemName,
    seriesName = seriesName,
    seasonNumber = seasonNumber,
    episodeNumber = episodeNumber,
    mediaType = mediaType.name,
    startedAt = startedAt,
    playDurationSeconds = playDurationSeconds,
    progressPercent = progressPercent,
    completed = completed,
    client = client,
    deviceName = deviceName,
    platform = platform,
    playMethod = playMethod?.name,
    videoCodec = videoCodec,
    audioCodec = audioCodec,
    resolution = resolution,
)

fun SessionHistoryPage.toDto(page: Int, pageSize: Int) = SessionHistoryPageDto(
    items = items.map { it.toDto() },
    page = page,
    pageSize = pageSize,
    totalPages = ((totalCount + pageSize - 1) / pageSize).toInt(),
    totalCount = totalCount,
)

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
