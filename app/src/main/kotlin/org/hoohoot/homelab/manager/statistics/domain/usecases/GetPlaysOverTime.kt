package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.TimeGranularity
import org.hoohoot.homelab.manager.statistics.domain.TimePoint
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries
import java.time.LocalDateTime

data class PlaysOverTime(val granularity: TimeGranularity, val points: List<TimePoint>)

@ApplicationScoped
class GetPlaysOverTime(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod): PlaysOverTime {
        val granularity = when (period) {
            StatsPeriod.TODAY -> TimeGranularity.HOUR
            StatsPeriod.THIS_WEEK, StatsPeriod.THIS_MONTH -> TimeGranularity.DAY
            StatsPeriod.THIS_YEAR, StatsPeriod.ALL_TIME -> TimeGranularity.MONTH
        }
        val range = periodResolver.resolve(period)
        val points = queries.playsOverTime(range, granularity)
        return PlaysOverTime(granularity, fillGaps(points, range.fromLocal, range.toLocal, granularity, range.allTime))
    }

    private fun fillGaps(
        points: List<TimePoint>,
        fromLocal: LocalDateTime,
        toLocal: LocalDateTime,
        granularity: TimeGranularity,
        allTime: Boolean,
    ): List<TimePoint> {
        val start = if (allTime) points.firstOrNull()?.bucketStart ?: return emptyList() else fromLocal
        val byBucket = points.associateBy { it.bucketStart }
        val filled = mutableListOf<TimePoint>()
        var bucket = truncate(start, granularity)
        while (bucket < toLocal) {
            filled += byBucket[bucket] ?: TimePoint(bucket, 0, 0)
            bucket = next(bucket, granularity)
        }
        return filled
    }

    private fun truncate(dateTime: LocalDateTime, granularity: TimeGranularity): LocalDateTime = when (granularity) {
        TimeGranularity.HOUR -> dateTime.withMinute(0).withSecond(0).withNano(0)
        TimeGranularity.DAY -> dateTime.toLocalDate().atStartOfDay()
        TimeGranularity.MONTH -> dateTime.toLocalDate().withDayOfMonth(1).atStartOfDay()
    }

    private fun next(bucket: LocalDateTime, granularity: TimeGranularity): LocalDateTime = when (granularity) {
        TimeGranularity.HOUR -> bucket.plusHours(1)
        TimeGranularity.DAY -> bucket.plusDays(1)
        TimeGranularity.MONTH -> bucket.plusMonths(1)
    }
}
