package org.hoohoot.homelab.manager.notifications.jellystat

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.notifications.jellystat.dto.ItemHistoryResponseDTO
import org.hoohoot.homelab.manager.notifications.jellystat.dto.ItemIdRequestDTO
import org.hoohoot.homelab.manager.notifications.jellystat.dto.PlayMediaStatisticsDTO
import org.hoohoot.homelab.manager.notifications.jellystat.dto.PopularMediaStatisticsDTO
import org.hoohoot.homelab.manager.notifications.jellystat.dto.UserActivityDTO

@RegisterRestClient(configKey = "jellystat-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "x-api-token", value = ["\${jellystat.api_token}"])
interface JellystatRestClient {
    @POST
    @Path("/stats/getMostPopularByType")
    suspend fun getMostPopularByType(@RequestBody request: StatisticsRequest): List<PopularMediaStatisticsDTO>

    @POST
    @Path("/stats/getMostViewedByType")
    suspend fun getMostViewedByType(@RequestBody request: StatisticsRequest): List<PlayMediaStatisticsDTO>

    @GET
    @Path("/stats/getAllUserActivity")
    suspend fun getAllUserActivity(): List<UserActivityDTO>

    @POST
    @Path("/api/getItemHistory")
    suspend fun getItemHistory(@RequestBody request: ItemIdRequestDTO, @QueryParam("page") page: Long, @QueryParam("size") size: Long): ItemHistoryResponseDTO
}
