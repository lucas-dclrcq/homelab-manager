package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.JellystatGateway
import org.hoohoot.homelab.manager.application.ports.JellystatMediaType

enum class TopWatchedPeriod(val days: Int) {
    LastWeek(7),
    LastMonth(30),
    LastYear(365)
}

data class TopWatched(val period: TopWatchedPeriod, val series: List<TopWatchedMedia>, val movies: List<TopWatchedMedia>)

data class TopWatchedMedia(val name: String, val viewers: Int)

data class GetTopWatched(val period: TopWatchedPeriod) : Query<TopWatched>

@Startup
@ApplicationScoped
class GetTopWatchedQueryHandler(private val jellystatGateway: JellystatGateway) :
    QueryHandler<GetTopWatched, TopWatched> {
    override suspend fun handle(query: GetTopWatched): TopWatched {
        val mostWatchedSeries = this.jellystatGateway
            .getMostPopularByType(query.period.days, JellystatMediaType.Series)
            .map { TopWatchedMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.viewers }

        val mostWatchedMovies = this.jellystatGateway
            .getMostPopularByType(query.period.days, JellystatMediaType.Movie)
            .map { TopWatchedMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.viewers }

        return TopWatched(query.period, mostWatchedSeries, mostWatchedMovies)
    }
}
