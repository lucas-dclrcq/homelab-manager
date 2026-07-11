package org.hoohoot.homelab.manager.shared.jellyfin

import com.fasterxml.jackson.annotation.JsonProperty

data class MediaSearchResult(val itemId: String, val name: String, val type: String)

data class JellyfinItemsResponse(
    @field:JsonProperty("Items")
    val items: List<JellyfinLibraryItem>? = null,

    @field:JsonProperty("TotalRecordCount")
    val totalRecordCount: Int? = null
)

data class JellyfinLibraryItem(
    @field:JsonProperty("Id")
    val id: String? = null,

    @field:JsonProperty("Name")
    val name: String? = null,

    @field:JsonProperty("ProductionYear")
    val productionYear: Int? = null,

    @field:JsonProperty("Type")
    val type: String? = null,

    @field:JsonProperty("ProviderIds")
    val providerIds: Map<String, String>? = null
)
