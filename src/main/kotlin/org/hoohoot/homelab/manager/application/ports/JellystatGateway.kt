package org.hoohoot.homelab.manager.application.ports

enum class JellystatMediaType { Movie, Series }

data class UniqueViewerStatistics(val uniqueViewers: Int, val name: String)

data class WatchEvent(val username: String, val episodeNumber: Int, val seasonNumber: Int, val episodeName: String)

interface JellystatGateway {
    suspend fun getMostPopularByType(lastNumberOfDays: Int, type: JellystatMediaType): List<UniqueViewerStatistics>
    suspend fun getMediaWatchEvents(itemId: String): List<WatchEvent>
}