package org.hoohoot.homelab.manager.cleanup.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.cleanup.domain.MovieWatchAggregate
import org.hoohoot.homelab.manager.cleanup.domain.SeasonWatchAggregate
import org.hoohoot.homelab.manager.cleanup.domain.ports.MemberStatuses
import org.hoohoot.homelab.manager.cleanup.domain.ports.PlaybackHistory
import org.hoohoot.homelab.manager.members.infra.MemberEntity
import java.time.LocalDateTime

// Agrégations SQL natives sur playback_session (timestamps UTC)
@ApplicationScoped
class PlaybackHistoryAdapter : PlaybackHistory, MemberStatuses {

    override suspend fun movieWatchAggregates(): List<MovieWatchAggregate> =
        queryRows(
            """SELECT item_id, MAX(item_name), MAX(ended_at), BOOL_OR(completed),
                      MAX(ended_at) FILTER (WHERE NOT completed AND progress_percent BETWEEN 5 AND 90)
               FROM playback_session
               WHERE media_type = 'MOVIE'
               GROUP BY item_id""",
        ).map { row ->
            MovieWatchAggregate(
                itemId = row[0] as String,
                itemName = row[1] as String,
                lastWatchedAt = row[2].asLocalDateTime(),
                completedBySomeone = row[3] == true,
                lastInProgressAt = row[4]?.asLocalDateTime(),
            )
        }

    override suspend fun seasonWatchAggregates(): List<SeasonWatchAggregate> =
        queryRows(
            """SELECT series_id, MAX(series_name), season_number, MAX(ended_at), BOOL_OR(completed),
                      MAX(ended_at) FILTER (WHERE NOT completed AND progress_percent BETWEEN 5 AND 90)
               FROM playback_session
               WHERE media_type = 'EPISODE'
               GROUP BY series_id, season_number""",
        ).map { row ->
            SeasonWatchAggregate(
                seriesId = row[0] as String?,
                seriesName = row[1] as String?,
                seasonNumber = (row[2] as Number?)?.toInt(),
                lastWatchedAt = row[3].asLocalDateTime(),
                completedBySomeone = row[4] == true,
                lastInProgressAt = row[5]?.asLocalDateTime(),
            )
        }

    override suspend fun userLastActivity(): Map<String, LocalDateTime> =
        queryRows(
            """SELECT LOWER(user_name), MAX(ended_at) FROM playback_session GROUP BY LOWER(user_name)""",
        ).associate { row -> row[0] as String to row[1].asLocalDateTime() }

    override suspend fun memberStatuses(): Map<String, Boolean> =
        Panache.withSession { MemberEntity.listAll() }.awaitSuspending()
            .associate { it.username.lowercase() to it.active }

    @Suppress("UNCHECKED_CAST")
    private suspend fun queryRows(sql: String): List<Array<Any?>> =
        Panache.withSession {
            Panache.getSession().flatMap { session ->
                session.createNativeQuery(sql.trimIndent(), Array::class.java).resultList
            }
        }.awaitSuspending().map { row ->
            if (row is Array<*>) row as Array<Any?> else arrayOf(row)
        }

    private fun Any?.asLocalDateTime(): LocalDateTime = when (this) {
        is LocalDateTime -> this
        is java.sql.Timestamp -> toLocalDateTime()
        else -> error("Type de timestamp inattendu : ${this?.javaClass}")
    }
}
