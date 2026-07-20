package org.hoohoot.homelab.manager.statistics.api

import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.statistics.domain.MediaKind
import org.hoohoot.homelab.manager.statistics.domain.SortDirection
import org.hoohoot.homelab.manager.statistics.domain.StatsPeriod
import org.hoohoot.homelab.manager.statistics.domain.TopMediaSort
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetActivityByHour
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetActivityByWeekday
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetActivityHeatmap
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetNowPlaying
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetPlatformBreakdown
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetPlaysOverTime
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetQualityBreakdown
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetSessionHistory
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetStatisticsSummary
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetTopMedia
import org.hoohoot.homelab.manager.statistics.domain.usecases.GetTopUsers

@Path("/api/statistics")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Statistics")
class StatisticsResource(
    private val getStatisticsSummaryUseCase: GetStatisticsSummary,
    private val getTopUsersUseCase: GetTopUsers,
    private val getTopMediaUseCase: GetTopMedia,
    private val getActivityByWeekdayUseCase: GetActivityByWeekday,
    private val getActivityByHourUseCase: GetActivityByHour,
    private val getActivityHeatmapUseCase: GetActivityHeatmap,
    private val getPlatformBreakdownUseCase: GetPlatformBreakdown,
    private val getPlaysOverTimeUseCase: GetPlaysOverTime,
    private val getQualityBreakdownUseCase: GetQualityBreakdown,
    private val getSessionHistoryUseCase: GetSessionHistory,
    private val getNowPlayingUseCase: GetNowPlaying,
) {

    @GET
    @Path("/summary")
    suspend fun getSummary(@QueryParam("period") period: StatsPeriod?): StatisticsSummaryDto =
        getStatisticsSummaryUseCase(period.orDefault()).toDto()

    @GET
    @Path("/top-users")
    suspend fun getTopUsers(@QueryParam("period") period: StatsPeriod?): List<TopUserDto> =
        getTopUsersUseCase(period.orDefault()).map { it.toDto() }

    @GET
    @Path("/top-media")
    suspend fun getTopMedia(
        @QueryParam("period") period: StatsPeriod?,
        @QueryParam("type") type: MediaKind?,
        @QueryParam("sort") sort: TopMediaSort?,
        @QueryParam("order") order: SortDirection?,
    ): List<TopMediaDto> =
        getTopMediaUseCase(
            period = period.orDefault(),
            kind = type ?: MediaKind.SERIES,
            sort = sort ?: TopMediaSort.PLAYS,
            direction = order ?: SortDirection.DESC,
        ).map { it.toDto() }

    @GET
    @Path("/activity-by-weekday")
    suspend fun getActivityByWeekday(@QueryParam("period") period: StatsPeriod?): List<WeekdayActivityDto> =
        getActivityByWeekdayUseCase(period.orDefault()).map { it.toDto() }

    @GET
    @Path("/activity-by-hour")
    suspend fun getActivityByHour(@QueryParam("period") period: StatsPeriod?): List<HourActivityDto> =
        getActivityByHourUseCase(period.orDefault()).map { it.toDto() }

    @GET
    @Path("/platforms")
    suspend fun getPlatforms(@QueryParam("period") period: StatsPeriod?): List<PlatformShareDto> =
        getPlatformBreakdownUseCase(period.orDefault()).map { it.toDto() }

    @GET
    @Path("/plays-over-time")
    suspend fun getPlaysOverTime(@QueryParam("period") period: StatsPeriod?): PlaysOverTimeDto =
        getPlaysOverTimeUseCase(period.orDefault()).toDto()

    @GET
    @Path("/activity-heatmap")
    suspend fun getActivityHeatmap(@QueryParam("period") period: StatsPeriod?): List<HeatmapCellDto> =
        getActivityHeatmapUseCase(period.orDefault()).map { it.toDto() }

    @GET
    @Path("/quality")
    suspend fun getQuality(@QueryParam("period") period: StatsPeriod?): QualityBreakdownDto =
        getQualityBreakdownUseCase(period.orDefault()).toDto()

    @GET
    @Path("/history")
    suspend fun getHistory(
        @QueryParam("period") period: StatsPeriod?,
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("pageSize") @DefaultValue("20") pageSize: Int,
    ): SessionHistoryPageDto {
        val sanitizedPage = page.coerceAtLeast(0)
        val sanitizedPageSize = pageSize.coerceIn(1, GetSessionHistory.MAX_PAGE_SIZE)
        return getSessionHistoryUseCase(period.orDefault(), sanitizedPage, sanitizedPageSize)
            .toDto(sanitizedPage, sanitizedPageSize)
    }

    @GET
    @Path("/now-playing")
    fun getNowPlaying(): List<NowPlayingDto> = getNowPlayingUseCase().map { it.toDto() }

    private fun StatsPeriod?.orDefault(): StatsPeriod = this ?: StatsPeriod.THIS_WEEK
}
