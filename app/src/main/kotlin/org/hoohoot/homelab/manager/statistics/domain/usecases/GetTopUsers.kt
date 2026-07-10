package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.TopUser
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

@ApplicationScoped
class GetTopUsers(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod): List<TopUser> =
        queries.topUsers(periodResolver.resolve(period))
}
