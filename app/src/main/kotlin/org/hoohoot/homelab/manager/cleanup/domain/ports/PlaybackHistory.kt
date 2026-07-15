package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.MovieWatchAggregate
import org.hoohoot.homelab.manager.cleanup.domain.SeasonWatchAggregate
import java.time.LocalDateTime

interface PlaybackHistory {
    suspend fun movieWatchAggregates(): List<MovieWatchAggregate>

    suspend fun seasonWatchAggregates(): List<SeasonWatchAggregate>

    suspend fun userLastActivity(): Map<String, LocalDateTime>
}

interface MemberStatuses {
    suspend fun memberStatuses(): Map<String, Boolean>
}
