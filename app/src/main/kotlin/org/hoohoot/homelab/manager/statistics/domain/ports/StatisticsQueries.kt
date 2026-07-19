package org.hoohoot.homelab.manager.statistics.domain.ports

import org.hoohoot.homelab.manager.statistics.domain.HeatmapCell
import org.hoohoot.homelab.manager.statistics.domain.HourActivity
import org.hoohoot.homelab.manager.statistics.domain.MediaKind
import org.hoohoot.homelab.manager.statistics.domain.MostPopular
import org.hoohoot.homelab.manager.statistics.domain.MostViewed
import org.hoohoot.homelab.manager.statistics.domain.PlatformShare
import org.hoohoot.homelab.manager.statistics.domain.QualityBreakdown
import org.hoohoot.homelab.manager.statistics.domain.SeriesWatcher
import org.hoohoot.homelab.manager.statistics.domain.SessionHistoryPage
import org.hoohoot.homelab.manager.statistics.domain.SortDirection
import org.hoohoot.homelab.manager.statistics.domain.StatisticsSummary
import org.hoohoot.homelab.manager.statistics.domain.StatsRange
import org.hoohoot.homelab.manager.statistics.domain.TimeGranularity
import org.hoohoot.homelab.manager.statistics.domain.TimePoint
import org.hoohoot.homelab.manager.statistics.domain.TopMediaSort
import org.hoohoot.homelab.manager.statistics.domain.TopMovie
import org.hoohoot.homelab.manager.statistics.domain.TopSeries
import org.hoohoot.homelab.manager.statistics.domain.TopUser
import org.hoohoot.homelab.manager.statistics.domain.WeekdayActivity
import java.time.LocalDateTime

interface StatisticsQueries {
    suspend fun summary(range: StatsRange): StatisticsSummary
    suspend fun topUsers(range: StatsRange): List<TopUser>
    suspend fun topSeries(range: StatsRange, limit: Int, sort: TopMediaSort, direction: SortDirection): List<TopSeries>
    suspend fun topMovies(range: StatsRange, limit: Int, sort: TopMediaSort, direction: SortDirection): List<TopMovie>
    suspend fun activityByWeekday(range: StatsRange): List<WeekdayActivity>
    suspend fun activityByHour(range: StatsRange): List<HourActivity>
    suspend fun platformBreakdown(range: StatsRange): List<PlatformShare>
    suspend fun playsOverTime(range: StatsRange, granularity: TimeGranularity): List<TimePoint>
    suspend fun activityHeatmap(range: StatsRange): List<HeatmapCell>
    suspend fun qualityBreakdown(range: StatsRange): QualityBreakdown
    suspend fun sessionHistory(range: StatsRange, page: Int, pageSize: Int): SessionHistoryPage

    // Requêtes du bot Matrix / weekly report (bornes UTC brutes, pas de notion de période d'affichage)
    suspend fun mostPopular(fromUtc: LocalDateTime, toUtc: LocalDateTime, kind: MediaKind, limit: Int): List<MostPopular>
    suspend fun mostViewed(fromUtc: LocalDateTime, toUtc: LocalDateTime, kind: MediaKind, limit: Int): List<MostViewed>
    suspend fun seriesWatchers(seriesId: String): List<SeriesWatcher>
}
