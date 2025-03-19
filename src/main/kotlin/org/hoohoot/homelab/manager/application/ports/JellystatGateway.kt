package org.hoohoot.homelab.manager.application.ports

import kotlin.time.Duration

enum class JellystatMediaType { Movie, Series }

data class UniqueViewerStatistics(val uniqueViewers: Int, val name: String)
data class PlaysStatistics(val name: String, val plays: Int, val totalPlayback: Duration)

data class WatchEvent(val username: String, val episodeNumber: Int, val seasonNumber: Int, val episodeName: String)

data class UserActivity(val username: String, val plays: Int, val totalPlayback: Duration)

interface JellystatGateway {
    suspend fun getMostPopularByType(lastNumberOfDays: Int, type: JellystatMediaType): List<UniqueViewerStatistics>
    suspend fun getMostViewedByType(lastNumberOfDays: Int, type: JellystatMediaType): List<PlaysStatistics>
    suspend fun getMediaWatchEvents(itemId: String): List<WatchEvent>
    suspend fun getAllUserActivity(): List<UserActivity>
}