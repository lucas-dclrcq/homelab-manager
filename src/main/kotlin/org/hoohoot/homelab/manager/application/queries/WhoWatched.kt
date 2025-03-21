package org.hoohoot.homelab.manager.application.queries

import com.trendyol.kediatr.Query
import com.trendyol.kediatr.QueryHandler
import io.quarkus.logging.Log
import io.quarkus.runtime.Startup
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.application.ports.JellyfinGateway
import org.hoohoot.homelab.manager.application.ports.JellystatGateway
import org.hoohoot.homelab.manager.application.ports.WatchEvent

data class WhoWatched(val tvShow: String) : Query<WhoWatchedInfos>

data class WhoWatchedInfos(val tvShow: String, val watchersCount: Int, val watchers: List<WatcherInfo>)
data class WatcherInfo(
    val username: String,
    val episodeWatchedCount: Int,
    val lastEpisodeWatched: String,
    val seasonNumber: Int,
    val episodeNumber: Int
)

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

        val firstFoundMedia = media.first()

        val mediaWatchEvents = this.jellystatGateway.getMediaWatchEvents(firstFoundMedia.itemId)

        return WhoWatchedInfos(
            firstFoundMedia.name,
            mediaWatchEvents.distinctWatcherCount(),
            mediaWatchEvents.watchersInformations()
        )
    }

    private fun List<WatchEvent>.distinctWatcherCount() = this.map { it.username }.distinct().count()

    private fun List<WatchEvent>.watchersInformations() = this.groupBy { it.username }
        .toList()
        .map { (username, events) ->
            val episodeWatchedCount = events.distinctBy { it.seasonNumber to it.episodeNumber }.count()
            val lastEpisodeWatched = events.sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber })).last()

            WatcherInfo(username, episodeWatchedCount, lastEpisodeWatched.episodeName, lastEpisodeWatched.seasonNumber, lastEpisodeWatched.episodeNumber)
        }
        .sortedWith(compareBy({ it.seasonNumber }, { it.episodeNumber }))
        .reversed()
}