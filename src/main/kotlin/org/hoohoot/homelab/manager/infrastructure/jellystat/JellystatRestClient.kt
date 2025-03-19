package org.hoohoot.homelab.manager.infrastructure.jellystat

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.infrastructure.jellystat.dto.PopularMediaStatistics

@RegisterRestClient(configKey = "jellystat-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "x-api-token", value = ["\${jellystat.api_token}"])
interface JellystatRestClient {
    @POST
    @Path("/stats/getMostPopularByType")
    suspend fun getMostPopularByType(@RequestBody request: StatisticsRequest): List<PopularMediaStatistics>

    @POST
    @Path("/stats/getMostViewedByType")
    suspend fun getMostViewedByType(@RequestBody request: StatisticsRequest): List<PlayMediaStatistics>

    @POST
    @Path("/api/getItemHistory")
    suspend fun getItemHistory(@RequestBody request: ItemIdRequest, @QueryParam("page") page: Long, @QueryParam("size") size: Long): ItemHistoryResponse
}

data class ItemIdRequest(@field:JsonProperty("itemid") val itemId: String)

data class ItemHistoryResponse(
    @field:JsonProperty("current_page")
    val currentPage: Long,
    val pages: Long,
    val size: Long,
    val sort: String,
    val desc: Boolean,
    val results: List<ItemHistoryResult>,
)

data class ItemHistoryResult(
    @field:JsonProperty("UserName")
    val userName: String? = null,
    @field:JsonProperty("EpisodeNumber")
    val episodeNumber: Long? = null,
    @field:JsonProperty("SeasonNumber")
    val seasonNumber: Long? = null,
    @field:JsonProperty("FullName")
    val fullName: String? = null,
)

data class PlayMediaStatistics(
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
