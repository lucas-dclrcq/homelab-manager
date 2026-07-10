package org.hoohoot.homelab.manager.statistics.domain.usecases

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.statistics.domain.MediaKind
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.SortDirection
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.TopMediaSort
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries

data class TopMediaItem(
    val name: String,
    val plays: Long,
    val watchTimeSeconds: Long,
    val uniqueViewers: Long,
    val completionRate: Double?,
    val bingeScore: Int?,
)

@ApplicationScoped
class GetTopMedia(
    private val queries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
) {
    suspend operator fun invoke(
        period: StatsPeriod,
        kind: MediaKind,
        sort: TopMediaSort = TopMediaSort.PLAYS,
        direction: SortDirection = SortDirection.DESC,
        limit: Int = 10,
    ): List<TopMediaItem> {
        val range = periodResolver.resolve(period)
        // Le binge score n'existe pas pour les films : retomber sur le tri par défaut
        val effectiveSort = if (kind == MediaKind.MOVIE && sort == TopMediaSort.BINGE_SCORE) TopMediaSort.PLAYS else sort
        return when (kind) {
            MediaKind.SERIES -> queries.topSeries(range, limit, effectiveSort, direction).map {
                TopMediaItem(
                    name = it.name,
                    plays = it.plays,
                    watchTimeSeconds = it.watchTimeSeconds,
                    uniqueViewers = it.uniqueViewers,
                    completionRate = it.completionRate,
                    bingeScore = it.bingeScore,
                )
            }
            MediaKind.MOVIE -> queries.topMovies(range, limit, effectiveSort, direction).map {
                TopMediaItem(
                    name = it.name,
                    plays = it.plays,
                    watchTimeSeconds = it.watchTimeSeconds,
                    uniqueViewers = it.uniqueViewers,
                    completionRate = it.completionRate,
                    bingeScore = null,
                )
            }
        }
    }
}
