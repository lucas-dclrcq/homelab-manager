package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.JellyfinGateway
import org.hoohoot.homelab.manager.application.ports.JellystatGateway

data class WhoWatched(val tvShow: String) : Query<WhoWatchedInfos>

data class WhoWatchedInfos(val tvShow: String, val watchersCount: Int, val watchers: List<WatcherInfo>)
data class WatcherInfo(val username: String, val episodeWatchedCount: Int, val lastEpisodeWatched: String)

data class NoSeriesFoundException(override val message: String) : Exception(message)
data class MultipleSeriesFoundException(override val message: String) : Exception(message)

@Startup
@ApplicationScoped
class WhoWatchedQueryHandler(
    private val jellyfinGateway: JellyfinGateway,
    private val jellystatGateway: JellystatGateway
) : QueryHandler<WhoWatched, WhoWatchedInfos> {
    override suspend fun handle(query: WhoWatched): WhoWatchedInfos {
        Log.info("Finding out who watched ${query.tvShow}")

        val media = this.jellyfinGateway.searchSeries(query.tvShow)

        if (media.isEmpty()) {
            throw NoSeriesFoundException("No series found for '${query.tvShow}'")
        }

        if (media.size > 1) {
            throw MultipleSeriesFoundException("Multiple series found for '${query.tvShow}' : ${media.joinToString(",") { it.name }}. Please be more specific.")
        }

        val itemId = media.first().itemId
        val mediaWatchEvents = this.jellystatGateway.getMediaWatchEvents(itemId)

        val watcherCount = mediaWatchEvents.map { it.username }.distinct().count()

        val watchers = mediaWatchEvents
            .groupBy { it.username }
            .toList()
            .map { (username, events) ->
                val episodeWatchedCount = events.distinctBy { it.seasonNumber to it.episodeNumber }.count()
                val lastEpisodeWatched = events.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber })).last()

                WatcherInfo(username, episodeWatchedCount, lastEpisodeWatched.episodeName)
            }

        return WhoWatchedInfos(media.first().name, watcherCount, watchers)
    }
}