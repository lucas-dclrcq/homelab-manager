package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ItemHistoryResponseDTO(
    @field:JsonProperty("current_page")
    val currentPage: Long,
    val pages: Long,
    val size: Long,
    val sort: String,
    val desc: Boolean,
    val results: List<ItemHistoryResultDTO>,
)