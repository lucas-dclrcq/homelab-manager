package org.hoohoot.homelab.manager.notifications.infra.stats

import io.vertx.core.Vertx
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.domain.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.domain.MostViewedMedia
import org.hoohoot.homelab.manager.notifications.domain.TopWatched
import org.hoohoot.homelab.manager.notifications.domain.TopWatchedPeriod
import org.hoohoot.homelab.manager.notifications.domain.UserStatistics
import org.hoohoot.homelab.manager.notifications.domain.WatcherInfo
import org.hoohoot.homelab.manager.notifications.domain.WhoWatchedInfos
import org.hoohoot.homelab.manager.notifications.domain.ports.ViewingStats
import org.hoohoot.homelab.manager.shared.vertx.runOnSafeVertxContext
import org.hoohoot.homelab.manager.statistics.domain.MediaKind
import org.hoohoot.homelab.manager.statistics.domain.PeriodResolver
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

/**
 * Implémentation de ViewingStats sur l'historique local playback_session (module statistics),
 * en remplacement de l'API Jellystat. Chaque appel bascule sur un contexte Vertx safe :
 * les consommateurs (commandes du bot Matrix) tournent hors des dispatchers Quarkus.
 */
@ApplicationScoped
class LocalViewingStats(
    private val statisticsQueries: StatisticsQueries,
    private val periodResolver: PeriodResolver,
    private val vertx: Vertx,
) : ViewingStats {

    override suspend fun topMovies(lastDays: Int, limit: Int): List<MostPopularMedia> =
        runOnSafeVertxContext(vertx) { mostPopular(lastDays, MediaKind.MOVIE, limit) }

    override suspend fun topSeries(lastDays: Int, limit: Int): List<MostPopularMedia> =
        runOnSafeVertxContext(vertx) { mostPopular(lastDays, MediaKind.SERIES, limit) }

    override suspend fun getTopWatched(period: TopWatchedPeriod): TopWatched = runOnSafeVertxContext(vertx) {
        val (from, to) = lastDaysRange(period.days)
        TopWatched(
            period = period,
            mostPopularSeries = mostPopular(period.days, MediaKind.SERIES, TOP_LIMIT),
            mostPopularMovies = mostPopular(period.days, MediaKind.MOVIE, TOP_LIMIT),
            mostViewedSeries = statisticsQueries.mostViewed(from, to, MediaKind.SERIES, TOP_LIMIT).map {
                MostViewedMedia(it.name, it.plays.toInt(), it.watchTimeSeconds.toHours())
            },
            mostViewedMovies = statisticsQueries.mostViewed(from, to, MediaKind.MOVIE, TOP_LIMIT).map {
                MostViewedMedia(it.name, it.plays.toInt(), it.watchTimeSeconds.toHours())
            },
        )
    }

    override suspend fun getTopWatchers(limit: Int): List<UserStatistics> = runOnSafeVertxContext(vertx) {
        statisticsQueries.topUsers(periodResolver.resolve(StatsPeriod.ALL_TIME))
            .take(limit)
            .map { UserStatistics(it.userName, it.playCount.toInt(), it.watchTimeSeconds.toHours()) }
    }

    override suspend fun getWatchersInfo(seriesId: String, seriesName: String): WhoWatchedInfos =
        runOnSafeVertxContext(vertx) {
            val watchers = statisticsQueries.seriesWatchers(seriesId)
                .map {
                    WatcherInfo(
                        username = it.userName,
                        episodeWatchedCount = it.episodesWatched.toInt(),
                        lastEpisodeWatched = it.lastEpisodeName,
                        seasonNumber = it.lastSeasonNumber ?: 0,
                        episodeNumber = it.lastEpisodeNumber ?: 0,
                    )
                }
                .sortedWith(compareByDescending<WatcherInfo> { it.seasonNumber }.thenByDescending { it.episodeNumber })
            WhoWatchedInfos(seriesName, watchers.size, watchers)
        }

    private suspend fun mostPopular(lastDays: Int, kind: MediaKind, limit: Int): List<MostPopularMedia> {
        val (from, to) = lastDaysRange(lastDays)
        return statisticsQueries.mostPopular(from, to, kind, limit)
            .map { MostPopularMedia(it.name, it.uniqueViewers.toInt()) }
    }

    private fun lastDaysRange(lastDays: Int): Pair<LocalDateTime, LocalDateTime> {
        val now = Instant.now()
        return LocalDateTime.ofInstant(now.minus(lastDays.toLong(), ChronoUnit.DAYS), ZoneOffset.UTC) to
            LocalDateTime.ofInstant(now.plus(1, ChronoUnit.HOURS), ZoneOffset.UTC)
    }

    private fun Long.toHours(): String = seconds.toString(DurationUnit.HOURS)

    companion object {
        private const val TOP_LIMIT = 10
    }
}
