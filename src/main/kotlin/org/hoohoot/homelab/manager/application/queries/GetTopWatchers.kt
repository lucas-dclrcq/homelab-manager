package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.JellystatGateway
import kotlin.time.DurationUnit

data class UserStatistics(val username: String, val totalPlays: Int, val totalPlaybackInHours: String)

data class GetTopWatchers(val size: Int = 10) : Query<List<UserStatistics>>

@Startup
@ApplicationScoped
class GetTopWatchersQueryHandler(private val jellystatGateway: JellystatGateway) : QueryHandler<GetTopWatchers, List<UserStatistics>> {
    override suspend fun handle(query: GetTopWatchers): List<UserStatistics> =
        this.jellystatGateway.getAllUserActivity()
            .sortedByDescending { it.totalPlayback }
            .take(query.size)
            .map { UserStatistics(it.username, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }
}
