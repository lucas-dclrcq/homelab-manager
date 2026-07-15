package org.hoohoot.homelab.manager.cleanup.api

import io.quarkus.logging.Log
import io.quarkus.vertx.core.runtime.context.VertxContextSafetyToggle
import io.smallrye.common.vertx.VertxContext
import io.vertx.core.Vertx
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.cleanup.domain.Accessor
import org.hoohoot.homelab.manager.cleanup.domain.CampaignTrigger
import org.hoohoot.homelab.manager.cleanup.domain.ScanResult
import org.hoohoot.homelab.manager.cleanup.domain.ports.Campaigns
import org.hoohoot.homelab.manager.cleanup.domain.ports.Candidates
import org.hoohoot.homelab.manager.cleanup.domain.usecases.CancelCampaign
import org.hoohoot.homelab.manager.cleanup.domain.usecases.GetCampaign
import org.hoohoot.homelab.manager.cleanup.domain.usecases.GetCleanupConfig
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ListCampaigns
import org.hoohoot.homelab.manager.cleanup.domain.usecases.RetryCandidate
import org.hoohoot.homelab.manager.cleanup.domain.usecases.ScanAndStartCampaign
import org.hoohoot.homelab.manager.cleanup.domain.usecases.UnprotectMedia
import org.hoohoot.homelab.manager.jobs.CleanupScanJob
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.vertx.VertxContextDispatcher
import java.util.UUID

@Path("/api/admin/cleanup")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name = "Admin Cleanup")
class AdminCleanupResource(
    private val getCleanupConfig: GetCleanupConfig,
    private val listCampaigns: ListCampaigns,
    private val getCampaign: GetCampaign,
    private val cancelCampaign: CancelCampaign,
    private val retryCandidate: RetryCandidate,
    private val unprotectMedia: UnprotectMedia,
    private val scanAndStartCampaign: ScanAndStartCampaign,
    private val campaigns: Campaigns,
    private val candidates: Candidates,
    private val vertx: Vertx,
) {

    @GET
    @Path("/config")
    suspend fun getConfig(): CleanupConfigDto = getCleanupConfig().toDto()

    @GET
    @Path("/campaigns")
    suspend fun listAllCampaigns(): List<CleanupCampaignSummaryDto> =
        listCampaigns().map { it.toSummaryDto() }

    @GET
    @Path("/campaigns/{id}")
    suspend fun getCampaignDetails(@PathParam("id") id: UUID): CleanupCampaignDetailsDto =
        getCampaign(id)?.let { it.campaign.toDetailsDto(it.candidates) }
            ?: throw NotFoundException()

    @POST
    @Path("/campaigns/scan")
    @APIResponseSchema(value = ScanStartedDto::class, responseCode = "202")
    suspend fun forceScan(request: ForceScanRequest?): Response {
        if (campaigns.findActive() != null) {
            return conflict("une campagne est déjà en cours")
        }
        launchInBackground {
            when (val result = scanAndStartCampaign(CampaignTrigger.MANUAL, request?.targetBytes)) {
                is ScanResult.Started ->
                    Log.info("Cleanup: manual campaign ${result.campaign.id} started with ${result.candidateCount} candidates")
                else -> Log.info("Cleanup: manual scan finished without campaign ($result)")
            }
        }
        return Response.accepted(ScanStartedDto(CleanupScanJob.IDENTITY)).build()
    }

    @POST
    @Path("/campaigns/{id}/cancel")
    @APIResponseSchema(value = CleanupCampaignDetailsDto::class, responseCode = "200")
    suspend fun cancel(@PathParam("id") id: UUID): Response =
        cancelCampaign(id).toResponse(candidates.listByCampaign(id))

    @POST
    @Path("/candidates/{id}/retry")
    @APIResponseSchema(value = CleanupCandidateDto::class, responseCode = "200")
    suspend fun retry(@PathParam("id") id: UUID): Response =
        retryCandidate(id).toResponse()

    @DELETE
    @Path("/protections/{id}")
    suspend fun deleteProtection(@PathParam("id") id: UUID): Response =
        unprotectMedia(id, Accessor.Admin).toResponse()

    private fun launchInBackground(block: suspend () -> Unit) {
        val context = VertxContext.getOrCreateDuplicatedContext(vertx)
        VertxContextSafetyToggle.setContextSafe(context, true)
        CoroutineScope(SupervisorJob() + VertxContextDispatcher(context)).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.error("Cleanup: manual scan failed", e)
            }
        }
    }
}
