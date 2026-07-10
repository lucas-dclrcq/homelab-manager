package org.hoohoot.homelab.manager.shared.jellyfin

import com.fasterxml.jackson.annotation.JsonProperty

data class JellyfinSessionDto(
    @field:JsonProperty("Id")
    val id: String? = null,

    @field:JsonProperty("UserId")
    val userId: String? = null,

    @field:JsonProperty("UserName")
    val userName: String? = null,

    @field:JsonProperty("Client")
    val client: String? = null,

    @field:JsonProperty("DeviceName")
    val deviceName: String? = null,

    @field:JsonProperty("DeviceId")
    val deviceId: String? = null,

    @field:JsonProperty("PlayState")
    val playState: JellyfinPlayState? = null,

    @field:JsonProperty("NowPlayingItem")
    val nowPlayingItem: JellyfinNowPlayingItem? = null,
)

data class JellyfinPlayState(
    @field:JsonProperty("IsPaused")
    val isPaused: Boolean? = null,

    @field:JsonProperty("PositionTicks")
    val positionTicks: Long? = null,
)

data class JellyfinNowPlayingItem(
    @field:JsonProperty("Id")
    val id: String? = null,

    @field:JsonProperty("Name")
    val name: String? = null,

    @field:JsonProperty("Type")
    val type: String? = null,

    @field:JsonProperty("RunTimeTicks")
    val runTimeTicks: Long? = null,

    @field:JsonProperty("SeriesId")
    val seriesId: String? = null,

    @field:JsonProperty("SeriesName")
    val seriesName: String? = null,

    @field:JsonProperty("ParentIndexNumber")
    val parentIndexNumber: Int? = null,

    @field:JsonProperty("IndexNumber")
    val indexNumber: Int? = null,
)
