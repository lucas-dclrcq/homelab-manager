package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.WeekdayActivity
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

@ApplicationScoped
class GetActivityByWeekday(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod): List<WeekdayActivity> {
        val byDay = queries.activityByWeekday(periodResolver.resolve(period)).associateBy { it.isoDayOfWeek }
        return (1..7).map { day -> byDay[day] ?: WeekdayActivity(day, 0, 0) }
    }
}
