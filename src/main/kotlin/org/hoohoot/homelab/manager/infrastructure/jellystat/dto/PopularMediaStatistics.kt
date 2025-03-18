package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PopularMediaStatistics(
    @JsonProperty("unique_viewers")
    val uniqueViewers: String? = null,

    @JsonProperty("latest_activity_date")
    val latestActivityDate: String? = null,

    @JsonProperty("Name")
    val name: String? = null,

    @JsonProperty("Id")
    val id: String? = null,

    @JsonProperty("PrimaryImageHash")
    val primaryImageHash: String? = null,

    @JsonProperty("archived")
    val archived: Boolean? = null
)