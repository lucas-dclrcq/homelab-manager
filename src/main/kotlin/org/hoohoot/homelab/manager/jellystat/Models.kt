package org.hoohoot.homelab.manager.jellystat

import kotlin.time.Duration

enum class JellystatMediaType { Movie, Series }

data class UniqueViewerStatistics(val uniqueViewers: Int, val name: String)
data class PlaysStatistics(val name: String, val plays: Int, val totalPlayback: Duration)

data class WatchEvent(val username: String, val episodeNumber: Int, val seasonNumber: Int, val episodeName: String)

data class UserActivity(val username: String, val plays: Int, val totalPlayback: Duration)
