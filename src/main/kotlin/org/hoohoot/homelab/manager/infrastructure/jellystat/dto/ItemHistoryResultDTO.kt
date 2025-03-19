package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ItemHistoryResultDTO(
    @field:JsonProperty("UserName")
    val userName: String? = null,
    @field:JsonProperty("EpisodeNumber")
    val episodeNumber: Long? = null,
    @field:JsonProperty("SeasonNumber")
    val seasonNumber: Long? = null,
    @field:JsonProperty("FullName")
    val fullName: String? = null,
)