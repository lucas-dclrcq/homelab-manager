package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.SessionHistoryPage
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

@ApplicationScoped
class GetSessionHistory(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod, page: Int, pageSize: Int): SessionHistoryPage =
        queries.sessionHistory(
            periodResolver.resolve(period),
            page = page.coerceAtLeast(0),
            pageSize = pageSize.coerceIn(1, MAX_PAGE_SIZE),
        )

    companion object {
        const val MAX_PAGE_SIZE = 100
    }
}
