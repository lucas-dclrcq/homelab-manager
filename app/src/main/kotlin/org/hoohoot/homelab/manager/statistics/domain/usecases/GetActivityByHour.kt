package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.HourActivity
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

@ApplicationScoped
class GetActivityByHour(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod): List<HourActivity> {
        val byHour = queries.activityByHour(periodResolver.resolve(period)).associateBy { it.hour }
        return (0..23).map { hour -> byHour[hour] ?: HourActivity(hour, 0, 0) }
    }
}
