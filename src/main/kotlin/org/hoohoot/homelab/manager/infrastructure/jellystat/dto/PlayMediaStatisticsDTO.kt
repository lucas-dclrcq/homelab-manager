package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class PlayMediaStatisticsDTO(
    @field:JsonProperty("Plays")
    val plays: String? = null,

    @field:JsonProperty("total_playback_duration")
    val totalPlaybackDuration: String? = null,

    @field:JsonProperty("Name")
    val name: String? = null,

    @field:JsonProperty("Id")
    val id: String? = null,

    @field:JsonProperty("PrimaryImageHash")
    val primaryImageHash: String? = null,

    @field:JsonProperty("archived")
    val archived: Boolean? = null
)