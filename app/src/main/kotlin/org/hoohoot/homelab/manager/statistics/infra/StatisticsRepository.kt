package org.hoohoot.homelab.manager.statistics.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hoohoot.homelab.manager.statistics.domain.HourActivity
import org.hoohoot.homelab.manager.statistics.domain.MediaKind
import org.hoohoot.homelab.manager.statistics.domain.MostPopular
import org.hoohoot.homelab.manager.statistics.domain.MostViewed
import org.hoohoot.homelab.manager.statistics.domain.PlatformShare
import org.hoohoot.homelab.manager.statistics.domain.Platforms
import org.hoohoot.homelab.manager.statistics.domain.SeriesWatcher
import org.hoohoot.homelab.manager.statistics.domain.SortDirection
import org.hoohoot.homelab.manager.statistics.domain.StatisticsSummary
import org.hoohoot.homelab.manager.statistics.domain.StatsRange
import org.hoohoot.homelab.manager.statistics.domain.TimeGranularity
import org.hoohoot.homelab.manager.statistics.domain.TimePoint
import org.hoohoot.homelab.manager.statistics.domain.TopMediaSort
import org.hoohoot.homelab.manager.statistics.domain.TopMovie
import org.hoohoot.homelab.manager.statistics.domain.TopSeries
import org.hoohoot.homelab.manager.statistics.domain.TopUser
import org.hoohoot.homelab.manager.statistics.domain.WeekdayActivity
import org.hoohoot.homelab.manager.statistics.domain.ports.StatisticsQueries
import java.time.LocalDateTime

/**
 * Agrégations SQL natives sur playback_session. La base stocke des timestamps UTC ;
 * les regroupements heure/jour utilisent le double AT TIME ZONE pour raisonner en heure locale.
 */
@ApplicationScoped
class StatisticsRepository(
    @param:ConfigProperty(name = "statistics.timezone") private val timezone: String,
) : StatisticsQueries {

    override suspend fun summary(range: StatsRange): StatisticsSummary {
        val totals = querySingle(
            """SELECT COALESCE(SUM(play_duration_seconds), 0), COUNT(*),
                      COUNT(DISTINCT (user_id, item_id)) FILTER (WHERE completed)
               FROM playback_session WHERE started_at >= :from AND started_at < :to""",
            range,
        )
        val peakHour = queryRows(
            """SELECT CAST(EXTRACT(HOUR FROM (started_at AT TIME ZONE 'UTC') AT TIME ZONE :tz) AS int) AS h, COUNT(*) AS plays
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY h ORDER BY plays DESC, h""",
            range,
            withTimezone = true,
            maxResults = 1,
        ).firstOrNull()

        return StatisticsSummary(
            totalWatchTimeSeconds = totals[0].asLong(),
            playCount = totals[1].asLong(),
            completedItems = totals[2].asLong(),
            peakHour = peakHour?.get(0)?.asInt(),
        )
    }

    override suspend fun topUsers(range: StatsRange): List<TopUser> =
        queryRows(
            """SELECT user_id, MAX(user_name), COALESCE(SUM(play_duration_seconds), 0),
                      COUNT(DISTINCT item_id), COUNT(*)
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY user_id ORDER BY 3 DESC""",
            range,
        ).map { row ->
            TopUser(
                userId = row[0] as String,
                userName = row[1] as String,
                watchTimeSeconds = row[2].asLong(),
                itemsWatched = row[3].asLong(),
                playCount = row[4].asLong(),
            )
        }

    override suspend fun topSeries(
        range: StatsRange,
        limit: Int,
        sort: TopMediaSort,
        direction: SortDirection,
    ): List<TopSeries> =
        queryRows(
            """WITH base AS (
                   SELECT * FROM playback_session
                   WHERE media_type = 'EPISODE' AND series_id IS NOT NULL
                     AND started_at >= :from AND started_at < :to
               ),
               daily AS (
                   SELECT series_id, user_id,
                          date_trunc('day', (started_at AT TIME ZONE 'UTC') AT TIME ZONE :tz) AS d,
                          COUNT(DISTINCT item_id) AS eps
                   FROM base GROUP BY series_id, user_id, d
               ),
               binge AS (SELECT series_id, AVG(eps) AS avg_eps FROM daily GROUP BY series_id)
               SELECT b.series_id, MAX(b.series_name) AS name, COUNT(*) AS plays,
                      COALESCE(SUM(b.play_duration_seconds), 0) AS watch_seconds,
                      COUNT(DISTINCT b.user_id) AS unique_viewers,
                      ROUND(100.0 * COUNT(DISTINCT (b.user_id, b.item_id)) FILTER (WHERE b.completed)
                            / NULLIF(COUNT(DISTINCT (b.user_id, b.item_id)), 0), 1) AS completion_rate,
                      CAST(LEAST(100, ROUND(binge.avg_eps / $BINGE_CAP_EPISODES_PER_DAY * 100)) AS int) AS binge_score
               FROM base b JOIN binge ON binge.series_id = b.series_id
               GROUP BY b.series_id, binge.avg_eps
               ${orderByClause(sort, direction)}""",
            range,
            withTimezone = true,
            maxResults = limit,
        ).map { row ->
            TopSeries(
                seriesId = row[0] as String,
                name = row[1] as String,
                plays = row[2].asLong(),
                watchTimeSeconds = row[3].asLong(),
                uniqueViewers = row[4].asLong(),
                completionRate = row[5]?.asDouble(),
                bingeScore = row[6].asInt(),
            )
        }

    override suspend fun topMovies(
        range: StatsRange,
        limit: Int,
        sort: TopMediaSort,
        direction: SortDirection,
    ): List<TopMovie> =
        queryRows(
            """SELECT item_id, MAX(item_name) AS name, COUNT(*) AS plays,
                      COALESCE(SUM(play_duration_seconds), 0) AS watch_seconds,
                      COUNT(DISTINCT user_id) AS unique_viewers,
                      ROUND(100.0 * COUNT(DISTINCT user_id) FILTER (WHERE completed)
                            / NULLIF(COUNT(DISTINCT user_id), 0), 1) AS completion_rate,
                      0 AS binge_score
               FROM playback_session
               WHERE media_type = 'MOVIE' AND started_at >= :from AND started_at < :to
               GROUP BY item_id ${orderByClause(sort, direction)}""",
            range,
            maxResults = limit,
        ).map { row ->
            TopMovie(
                itemId = row[0] as String,
                name = row[1] as String,
                plays = row[2].asLong(),
                watchTimeSeconds = row[3].asLong(),
                uniqueViewers = row[4].asLong(),
                completionRate = row[5]?.asDouble(),
            )
        }

    // Alias SQL pilotés par enum : pas d'injection possible
    private fun orderByClause(sort: TopMediaSort, direction: SortDirection): String {
        val column = when (sort) {
            TopMediaSort.NAME -> "name"
            TopMediaSort.PLAYS -> "plays"
            TopMediaSort.WATCH_TIME -> "watch_seconds"
            TopMediaSort.UNIQUE_VIEWERS -> "unique_viewers"
            TopMediaSort.COMPLETION_RATE -> "completion_rate"
            TopMediaSort.BINGE_SCORE -> "binge_score"
        }
        return "ORDER BY $column ${direction.name} NULLS LAST, name ASC"
    }

    override suspend fun activityByWeekday(range: StatsRange): List<WeekdayActivity> =
        queryRows(
            """SELECT CAST(EXTRACT(ISODOW FROM (started_at AT TIME ZONE 'UTC') AT TIME ZONE :tz) AS int) AS d,
                      COUNT(*), COALESCE(SUM(play_duration_seconds), 0)
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY d ORDER BY d""",
            range,
            withTimezone = true,
        ).map { row -> WeekdayActivity(row[0].asInt(), row[1].asLong(), row[2].asLong()) }

    override suspend fun activityByHour(range: StatsRange): List<HourActivity> =
        queryRows(
            """SELECT CAST(EXTRACT(HOUR FROM (started_at AT TIME ZONE 'UTC') AT TIME ZONE :tz) AS int) AS h,
                      COUNT(*), COALESCE(SUM(play_duration_seconds), 0)
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY h ORDER BY h""",
            range,
            withTimezone = true,
        ).map { row -> HourActivity(row[0].asInt(), row[1].asLong(), row[2].asLong()) }

    override suspend fun platformBreakdown(range: StatsRange): List<PlatformShare> =
        queryRows(
            """SELECT COALESCE(platform, '${Platforms.OTHER}'), COUNT(*), COALESCE(SUM(play_duration_seconds), 0)
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY 1 ORDER BY 2 DESC""",
            range,
        ).map { row -> PlatformShare(row[0] as String, row[1].asLong(), row[2].asLong()) }

    override suspend fun playsOverTime(range: StatsRange, granularity: TimeGranularity): List<TimePoint> =
        queryRows(
            """SELECT date_trunc(:granularity, (started_at AT TIME ZONE 'UTC') AT TIME ZONE :tz) AS bucket,
                      COUNT(*), COALESCE(SUM(play_duration_seconds), 0)
               FROM playback_session WHERE started_at >= :from AND started_at < :to
               GROUP BY bucket ORDER BY bucket""",
            range,
            withTimezone = true,
            granularity = granularity,
        ).map { row -> TimePoint(row[0].asLocalDateTime(), row[1].asLong(), row[2].asLong()) }

    override suspend fun mostPopular(fromUtc: LocalDateTime, toUtc: LocalDateTime, kind: MediaKind, limit: Int): List<MostPopular> =
        queryRows(
            when (kind) {
                MediaKind.SERIES ->
                    """SELECT MAX(series_name), COUNT(DISTINCT user_id) AS viewers
                       FROM playback_session
                       WHERE media_type = 'EPISODE' AND series_id IS NOT NULL
                         AND started_at >= :from AND started_at < :to
                       GROUP BY series_id ORDER BY viewers DESC"""
                MediaKind.MOVIE ->
                    """SELECT MAX(item_name), COUNT(DISTINCT user_id) AS viewers
                       FROM playback_session
                       WHERE media_type = 'MOVIE' AND started_at >= :from AND started_at < :to
                       GROUP BY item_id ORDER BY viewers DESC"""
            },
            mapOf("from" to fromUtc, "to" to toUtc),
            maxResults = limit,
        ).map { row -> MostPopular(row[0] as String, row[1].asLong()) }

    override suspend fun mostViewed(fromUtc: LocalDateTime, toUtc: LocalDateTime, kind: MediaKind, limit: Int): List<MostViewed> =
        queryRows(
            when (kind) {
                MediaKind.SERIES ->
                    """SELECT MAX(series_name), COUNT(*) AS plays, COALESCE(SUM(play_duration_seconds), 0)
                       FROM playback_session
                       WHERE media_type = 'EPISODE' AND series_id IS NOT NULL
                         AND started_at >= :from AND started_at < :to
                       GROUP BY series_id ORDER BY plays DESC"""
                MediaKind.MOVIE ->
                    """SELECT MAX(item_name), COUNT(*) AS plays, COALESCE(SUM(play_duration_seconds), 0)
                       FROM playback_session
                       WHERE media_type = 'MOVIE' AND started_at >= :from AND started_at < :to
                       GROUP BY item_id ORDER BY plays DESC"""
            },
            mapOf("from" to fromUtc, "to" to toUtc),
            maxResults = limit,
        ).map { row -> MostViewed(row[0] as String, row[1].asLong(), row[2].asLong()) }

    override suspend fun seriesWatchers(seriesId: String): List<SeriesWatcher> =
        queryRows(
            """SELECT DISTINCT ON (p.user_name) p.user_name, counts.eps, p.item_name, p.season_number, p.episode_number
               FROM playback_session p
               JOIN (SELECT user_name, COUNT(DISTINCT item_id) AS eps
                     FROM playback_session
                     WHERE series_id = :seriesId AND media_type = 'EPISODE'
                     GROUP BY user_name) counts ON counts.user_name = p.user_name
               WHERE p.series_id = :seriesId AND p.media_type = 'EPISODE'
               ORDER BY p.user_name, p.season_number DESC NULLS LAST, p.episode_number DESC NULLS LAST""",
            mapOf("seriesId" to seriesId),
        ).map { row ->
            SeriesWatcher(
                userName = row[0] as String,
                episodesWatched = row[1].asLong(),
                lastEpisodeName = row[2] as String,
                lastSeasonNumber = row[3]?.asInt(),
                lastEpisodeNumber = row[4]?.asInt(),
            )
        }

    private suspend fun querySingle(sql: String, range: StatsRange): Array<Any?> =
        queryRows(sql, range).first()

    private suspend fun queryRows(
        sql: String,
        range: StatsRange,
        withTimezone: Boolean = false,
        granularity: TimeGranularity? = null,
        maxResults: Int? = null,
    ): List<Array<Any?>> {
        val params = buildMap {
            put("from", range.fromUtc)
            put("to", range.toUtc)
            if (withTimezone) put("tz", timezone)
            granularity?.let { put("granularity", it.name.lowercase()) }
        }
        return queryRows(sql, params, maxResults)
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun queryRows(
        sql: String,
        params: Map<String, Any>,
        maxResults: Int? = null,
    ): List<Array<Any?>> =
        Panache.withSession {
            Panache.getSession().flatMap { session ->
                val query = session.createNativeQuery(sql.trimIndent(), Array::class.java)
                params.forEach { (name, value) -> query.setParameter(name, value) }
                maxResults?.let { query.setMaxResults(it) }
                query.resultList
            }
        }.awaitSuspending().map { row ->
            // Une seule colonne sélectionnée => Hibernate renvoie la valeur nue, sinon un Array
            if (row is Array<*>) row as Array<Any?> else arrayOf(row)
        }

    private fun Any?.asLong(): Long = (this as Number).toLong()
    private fun Any?.asInt(): Int = (this as Number).toInt()
    private fun Any?.asDouble(): Double = (this as Number).toDouble()
    private fun Any?.asLocalDateTime(): LocalDateTime = when (this) {
        is LocalDateTime -> this
        is java.sql.Timestamp -> toLocalDateTime()
        else -> error("Type de bucket inattendu: ${this?.javaClass}")
    }

    companion object {
        /** Binge score = min(100, moyenne d'épisodes distincts vus par jour actif et par user ÷ cap × 100). */
        private const val BINGE_CAP_EPISODES_PER_DAY = 6.0
    }
}
