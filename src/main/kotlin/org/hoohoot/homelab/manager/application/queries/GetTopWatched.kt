package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.JellystatGateway
import org.hoohoot.homelab.manager.application.ports.JellystatMediaType
import kotlin.time.DurationUnit

enum class TopWatchedPeriod(val days: Int) {
    LastWeek(7),
    LastMonth(30),
    LastYear(365)
}

data class TopWatched(
    val period: TopWatchedPeriod,
    val mostPopularSeries: List<MostPopularMedia>,
    val mostPopularMovies: List<MostPopularMedia>,
    val mostViewedSeries: List<MostViewedMedia>,
    val mostViewedMovies: List<MostViewedMedia>
)

data class MostPopularMedia(val name: String, val uniqueViewers: Int)
data class MostViewedMedia(val name: String, val plays: Int, val totalPlaybackInHours: String)

data class GetTopWatched(val period: TopWatchedPeriod) : Query<TopWatched>

@Startup
@ApplicationScoped
class GetTopWatchedQueryHandler(private val jellystatGateway: JellystatGateway) :
    QueryHandler<GetTopWatched, TopWatched> {
    override suspend fun handle(query: GetTopWatched): TopWatched {
        val mostPopularSeries = this.jellystatGateway
            .getMostPopularByType(query.period.days, JellystatMediaType.Series)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }

        val mostPopularMovies = this.jellystatGateway
            .getMostPopularByType(query.period.days, JellystatMediaType.Movie)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }

        val mostViewedSeries = this.jellystatGateway
            .getMostViewedByType(query.period.days, JellystatMediaType.Series)
            .map { MostViewedMedia(it.name, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }
            .sortedByDescending { it.plays }

        val mostViewedMovies = this.jellystatGateway
            .getMostViewedByType(query.period.days, JellystatMediaType.Movie)
            .map { MostViewedMedia(it.name, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }
            .sortedByDescending { it.plays }

        return TopWatched(query.period, mostPopularSeries, mostPopularMovies, mostViewedSeries, mostViewedMovies)
    }
}
