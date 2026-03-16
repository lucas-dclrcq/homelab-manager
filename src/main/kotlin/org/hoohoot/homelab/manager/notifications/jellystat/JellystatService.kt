package org.hoohoot.homelab.manager.notifications.jellystat

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.jellystat.dto.ItemIdRequestDTO
import org.hoohoot.homelab.manager.media.*
import kotlin.time.Duration
import kotlin.time.DurationUnit

@ApplicationScoped
class JellystatService(@RestClient private val jellystatRestClient: JellystatRestClient) {
    suspend fun getMostPopularByType(
        lastNumberOfDays: Int,
        type: JellystatMediaType
    ): List<UniqueViewerStatistics> =
        jellystatRestClient.getMostPopularByType(StatisticsRequest(lastNumberOfDays.toString(), type.name))
            .map { UniqueViewerStatistics(it.uniqueViewers?.toInt() ?: 0, it.name ?: "unknown") }

    suspend fun getMostViewedByType(lastNumberOfDays: Int, type: JellystatMediaType): List<PlaysStatistics> =
        jellystatRestClient.getMostViewedByType(StatisticsRequest(lastNumberOfDays.toString(), type.name))
            .map {
                PlaysStatistics(
                    it.name ?: "unknown",
                    it.plays?.toInt() ?: 0,
                    it.totalPlaybackDuration.let { duration -> Duration.parse("${duration}s")
            })
}

    suspend fun getMediaWatchEvents(itemId: String): List<WatchEvent> {
        val watchEvents = mutableListOf<WatchEvent>();

        var page = 1L;
        var totalPages = 1L;

        do {
            Log.info("Getting watch events for item $itemId, page $page of $totalPages")
            val (currentPage, pages, _, _, _, results) = jellystatRestClient.getItemHistory(ItemIdRequestDTO(itemId), page, 50)

            watchEvents.addAll(
                results
                    .filter { it.userName != null && it.episodeNumber != null && it.seasonNumber != null && it.fullName != null }
                    .map {
                        WatchEvent(
                            it.userName!!,
                            it.episodeNumber!!.toInt(),
                            it.seasonNumber!!.toInt(),
                            it.fullName!!
                        )
                    })

            page = currentPage + 1
            totalPages = pages

        } while (page <= totalPages)

        return watchEvents.toList()
    }

    suspend fun getAllUserActivity(): List<UserActivity> {
        return this.jellystatRestClient.getAllUserActivity().map { UserActivity(
            it.userName ?: "unknown",
            it.totalPlays?.toInt() ?: 0,
            it.totalWatchTime.let { duration -> Duration.parse("${duration}s")}
        ) }
    }

    // High-level methods

    suspend fun getTopWatched(period: TopWatchedPeriod): TopWatched {
        val mostPopularSeries = getMostPopularByType(period.days, JellystatMediaType.Series)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }

        val mostPopularMovies = getMostPopularByType(period.days, JellystatMediaType.Movie)
            .map { MostPopularMedia(it.name, it.uniqueViewers) }
            .sortedByDescending { it.uniqueViewers }

        val mostViewedSeries = getMostViewedByType(period.days, JellystatMediaType.Series)
            .map { MostViewedMedia(it.name, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }
            .sortedByDescending { it.plays }

        val mostViewedMovies = getMostViewedByType(period.days, JellystatMediaType.Movie)
            .map { MostViewedMedia(it.name, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }
            .sortedByDescending { it.plays }

        return TopWatched(period, mostPopularSeries, mostPopularMovies, mostViewedSeries, mostViewedMovies)
    }

    suspend fun getTopWatchers(limit: Int): List<UserStatistics> =
        getAllUserActivity()
            .sortedByDescending { it.totalPlayback }
            .take(limit)
            .map { UserStatistics(it.username, it.plays, it.totalPlayback.toString(DurationUnit.HOURS)) }

    suspend fun getWatchersInfo(itemId: String, tvShowName: String): WhoWatchedInfos {
        val mediaWatchEvents = getMediaWatchEvents(itemId)

        return WhoWatchedInfos(
            tvShowName,
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
