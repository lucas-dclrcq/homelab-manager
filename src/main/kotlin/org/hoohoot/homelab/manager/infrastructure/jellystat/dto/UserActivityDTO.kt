package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UserActivityDTO(
    @field:JsonProperty("UserId")
    val userId: String? = null,

    @field:JsonProperty("PrimaryImageTag")
    val primaryImageTag: String? = null,

    @field:JsonProperty("UserName")
    val userName: String? = null,

    @field:JsonProperty("LastWatched")
    val lastWatched: String? = null,

    @field:JsonProperty("NowPlayingItemId")
    val nowPlayingItemId: String? = null,

    @field:JsonProperty("LastActivityDate")
    val lastActivityDate: String? = null,

    @field:JsonProperty("LastClient")
    val lastClient: String? = null,

    @field:JsonProperty("TotalPlays")
    val totalPlays: String? = null,

    @field:JsonProperty("TotalWatchTime")
    val totalWatchTime: String? = null,

    @field:JsonProperty("LastSeen")
    val lastSeen: LastSeenDTO? = null
)

data class LastSeenDTO(
    @field:JsonProperty("minutes")
    val minutes: Int? = null,

    @field:JsonProperty("seconds")
    val seconds: Int? = null,

    @field:JsonProperty("milliseconds")
    val milliseconds: Double? = null
)