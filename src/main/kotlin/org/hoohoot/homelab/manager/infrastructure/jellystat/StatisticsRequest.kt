package org.hoohoot.homelab.manager.infrastructure.jellystat

import com.fasterxml.jackson.annotation.JsonProperty

data class StatisticsRequest(
    @field:JsonProperty("days") val days: String? = null,
    @field:JsonProperty("type") val type: String? = null
)