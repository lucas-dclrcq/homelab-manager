package org.hoohoot.homelab.manager.notifications.jellystat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PopularMediaStatisticsDTO(
    @param:JsonProperty("unique_viewers")
    val uniqueViewers: String? = null,

    @param:JsonProperty("latest_activity_date")
    val latestActivityDate: String? = null,

    @param:JsonProperty("Name")
    val name: String? = null,

    @param:JsonProperty("Id")
    val id: String? = null,

    @param:JsonProperty("PrimaryImageHash")
    val primaryImageHash: String? = null,

    @param:JsonProperty("archived")
    val archived: Boolean? = null
)
