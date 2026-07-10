package org.hoohoot.homelab.manager.notifications.domain.ports

import org.hoohoot.homelab.manager.notifications.domain.MostPopularMedia
import org.hoohoot.homelab.manager.notifications.domain.TopWatched
import org.hoohoot.homelab.manager.notifications.domain.TopWatchedPeriod
import org.hoohoot.homelab.manager.notifications.domain.UserStatistics
import org.hoohoot.homelab.manager.notifications.domain.WhoWatchedInfos

interface ViewingStats {
    suspend fun topMovies(lastDays: Int, limit: Int): List<MostPopularMedia>
    suspend fun topSeries(lastDays: Int, limit: Int): List<MostPopularMedia>
    suspend fun getTopWatched(period: TopWatchedPeriod): TopWatched
    suspend fun getTopWatchers(limit: Int): List<UserStatistics>
    suspend fun getWatchersInfo(seriesId: String, seriesName: String): WhoWatchedInfos
}
