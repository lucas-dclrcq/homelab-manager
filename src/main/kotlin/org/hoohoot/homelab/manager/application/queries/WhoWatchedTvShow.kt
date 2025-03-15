package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped

data class WhoWatchedTvShow(val tvShow: String): Query<List<String>>

@Startup
@ApplicationScoped
class WhoWatchedTvShowQueryHandler : QueryHandler<WhoWatchedTvShow, List<String>> {
    override suspend fun handle(query: WhoWatchedTvShow): List<String> {
        return listOf(query.tvShow, "Alice", "Bob")
    }

}