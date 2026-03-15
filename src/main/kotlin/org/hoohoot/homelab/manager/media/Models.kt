package org.hoohoot.homelab.manager.media

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

data class UserStatistics(val username: String, val totalPlays: Int, val totalPlaybackInHours: String)

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
