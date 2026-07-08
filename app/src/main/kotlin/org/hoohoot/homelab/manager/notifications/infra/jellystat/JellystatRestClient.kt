package org.hoohoot.homelab.manager.notifications.infra.jellystat

import jakarta.ws.rs.ProcessingException
import java.time.temporal.ChronoUnit
import org.eclipse.microprofile.faulttolerance.Retry
import org.eclipse.microprofile.faulttolerance.Timeout
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.hoohoot.homelab.manager.notifications.infra.jellystat.dto.ItemHistoryResponseDTO
import org.hoohoot.homelab.manager.notifications.infra.jellystat.dto.ItemIdRequestDTO
import org.hoohoot.homelab.manager.notifications.infra.jellystat.dto.PlayMediaStatisticsDTO
import org.hoohoot.homelab.manager.notifications.infra.jellystat.dto.PopularMediaStatisticsDTO
import org.hoohoot.homelab.manager.notifications.infra.jellystat.dto.UserActivityDTO

@RegisterRestClient(configKey = "jellystat-api")
@Consumes(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "x-api-token", value = ["\${jellystat.api_token}"])
@Retry(maxRetries = 2, delay = 500, jitter = 250, retryOn = [ProcessingException::class, TimeoutException::class])
@Timeout(value = 30, unit = ChronoUnit.SECONDS)
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
