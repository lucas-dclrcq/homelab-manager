package org.hoohoot.homelab.manager.finances.api

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.usecases.GetCurrentEnergyStatus
import org.hoohoot.homelab.manager.finances.domain.usecases.GetFinanceSummary
import org.hoohoot.homelab.manager.finances.domain.usecases.GetMonthlyBreakdown
import org.hoohoot.homelab.manager.finances.domain.usecases.SearchFinanceEntries
import java.time.LocalDate

@Path("/api/finances")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Finances")
class FinancesResource(
    private val getFinanceSummaryUseCase: GetFinanceSummary,
    private val getMonthlyBreakdownUseCase: GetMonthlyBreakdown,
    private val searchFinanceEntriesUseCase: SearchFinanceEntries,
    private val getCurrentEnergyStatusUseCase: GetCurrentEnergyStatus,
) {

    @GET
    @Path("/summary")
    suspend fun getSummary(@QueryParam("year") year: Int?): FinanceSummaryDto {
        val effectiveYear = year ?: LocalDate.now().year
        return getFinanceSummaryUseCase(effectiveYear).toDto(effectiveYear)
    }

    @GET
    @Path("/monthly")
    suspend fun getMonthlyBreakdown(@QueryParam("year") year: Int?): List<MonthlyTotalsDto> =
        getMonthlyBreakdownUseCase(year ?: LocalDate.now().year).map { it.toDto() }

    @GET
    @Path("/entries")
    suspend fun searchEntries(
        @QueryParam("year") year: Int?,
        @QueryParam("type") type: EntryType?,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?,
    ): FinanceEntriesPageDto =
        searchFinanceEntriesUseCase(
            year = year ?: LocalDate.now().year,
            type = type,
            page = page ?: 0,
            pageSize = pageSize ?: 20,
        ).toDto()

    @GET
    @Path("/energy")
    suspend fun getEnergyStatus(): EnergyStatusDto {
        val status = getCurrentEnergyStatusUseCase()
        return EnergyStatusDto(
            currentPowerWatts = status.currentPowerWatts,
            estimatedMonthlyKwh = status.estimatedMonthlyKwh,
            estimatedMonthlyCostCents = status.estimatedMonthlyCostCents,
            kwhPriceConfigured = status.kwhPriceConfigured,
        )
    }
}
