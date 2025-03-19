package org.hoohoot.homelab.manager.infrastructure.jellystat.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ItemIdRequestDTO(@field:JsonProperty("itemid") val itemId: String)