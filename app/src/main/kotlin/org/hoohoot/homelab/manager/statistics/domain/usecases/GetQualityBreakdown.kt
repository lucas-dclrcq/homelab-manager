package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.QualityBreakdown
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

@ApplicationScoped
class GetQualityBreakdown(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(period: StatsPeriod): QualityBreakdown =
        queries.qualityBreakdown(periodResolver.resolve(period))
}
