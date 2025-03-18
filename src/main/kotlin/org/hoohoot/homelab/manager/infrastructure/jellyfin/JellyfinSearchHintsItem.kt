package org.hoohoot.homelab.manager.infrastructure.jellyfin

import com.fasterxml.jackson.annotation.JsonProperty

data class JellyfinSearchHintsItem(
    @field:JsonProperty("ItemId")
    val itemId: String? = null,

    @field:JsonProperty("Id")
    val id: String? = null,

    @field:JsonProperty("Name")
    val name: String? = null,

    @field:JsonProperty("ProductionYear")
    val productionYear: Int? = null,

    @field:JsonProperty("PrimaryImageTag")
    val primaryImageTag: String? = null,

    @field:JsonProperty("ThumbImageTag")
    val thumbImageTag: String? = null,

    @field:JsonProperty("ThumbImageItemId")
    val thumbImageItemId: String? = null,

    @field:JsonProperty("BackdropImageTag")
    val backdropImageTag: String? = null,

    @field:JsonProperty("BackdropImageItemId")
    val backdropImageItemId: String? = null,

    @field:JsonProperty("Type")
    val type: String? = null,

    @field:JsonProperty("IsFolder")
    val isFolder: Boolean? = null,

    @field:JsonProperty("RunTimeTicks")
    val runTimeTicks: Long? = null,

    @field:JsonProperty("MediaType")
    val mediaType: String? = null,

    @field:JsonProperty("EndDate")
    val endDate: String? = null,

    @field:JsonProperty("Status")
    val status: String? = null,

    @field:JsonProperty("Artists")
    val artists: List<String>? = null,

    @field:JsonProperty("ChannelId")
    val channelId: String? = null,

    @field:JsonProperty("PrimaryImageAspectRatio")
    val primaryImageAspectRatio: Double? = null
)

data class JellyfinSearchHintsResponse(
    @field:JsonProperty("SearchHints")
    val searchHints: List<JellyfinSearchHintsItem>? = null,

    @field:JsonProperty("TotalRecordCount")
    val totalRecordCount: Int? = null
)