package org.hoohoot.homelab.manager.notifications.infrastructure.jellystat

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(configKey = "jellystat-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "x-api-token", value = ["\${jellystat.api_token}"])
interface JellystatRestClient {
    @POST
    @Path("/stats/getMostPopularByType")
    suspend fun getMostPopularByType(@RequestBody request: StatisticsRequest): List<PopularMediaStatistics>
}

data class StatisticsRequest(
    val type: String,
    val days: String
)

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
