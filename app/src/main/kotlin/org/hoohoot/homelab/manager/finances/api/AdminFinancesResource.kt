package org.hoohoot.homelab.manager.finances.api

import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.eclipse.microprofile.openapi.annotations.responses.APIResponseSchema
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.finances.domain.EntryType
import org.hoohoot.homelab.manager.finances.domain.FinanceEntryInput
import org.hoohoot.homelab.manager.finances.domain.RecurringRuleInput
import org.hoohoot.homelab.manager.finances.domain.usecases.CreateFinanceEntry
import org.hoohoot.homelab.manager.finances.domain.usecases.CreateRecurringRule
import org.hoohoot.homelab.manager.finances.domain.usecases.DeleteFinanceEntry
import org.hoohoot.homelab.manager.finances.domain.usecases.DeleteRecurringRule
import org.hoohoot.homelab.manager.finances.domain.usecases.EntryDeleteResult
import org.hoohoot.homelab.manager.finances.domain.usecases.EntryWriteResult
import org.hoohoot.homelab.manager.finances.domain.usecases.GetFinanceSettings
import org.hoohoot.homelab.manager.finances.domain.usecases.ListRecurringRules
import org.hoohoot.homelab.manager.finances.domain.usecases.RuleWriteResult
import org.hoohoot.homelab.manager.finances.domain.usecases.UpdateFinanceEntry
import org.hoohoot.homelab.manager.finances.domain.usecases.UpdateFinanceSettings
import org.hoohoot.homelab.manager.finances.domain.usecases.UpdateRecurringRule
import org.hoohoot.homelab.manager.shared.api.badRequest
import org.hoohoot.homelab.manager.shared.api.conflict
import org.hoohoot.homelab.manager.shared.api.notFound
import java.math.BigDecimal
import java.util.UUID

@Path("/api/admin/finances")
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
@Tag(name = "Finances")
class AdminFinancesResource(
    private val createFinanceEntryUseCase: CreateFinanceEntry,
    private val updateFinanceEntryUseCase: UpdateFinanceEntry,
    private val deleteFinanceEntryUseCase: DeleteFinanceEntry,
    private val listRecurringRulesUseCase: ListRecurringRules,
    private val createRecurringRuleUseCase: CreateRecurringRule,
    private val updateRecurringRuleUseCase: UpdateRecurringRule,
    private val deleteRecurringRuleUseCase: DeleteRecurringRule,
    private val getFinanceSettingsUseCase: GetFinanceSettings,
    private val updateFinanceSettingsUseCase: UpdateFinanceSettings,
) {

    @POST
    @Path("/entries")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = FinanceEntryDto::class, responseCode = "201")
    suspend fun createEntry(request: FinanceEntryRequest): Response {
        val input = request.toInput() ?: return badRequest(entryValidationMessage(request))
        return when (val result = createFinanceEntryUseCase(input)) {
            is EntryWriteResult.Ok -> Response.status(Response.Status.CREATED).entity(result.entity.toDto()).build()
            EntryWriteResult.UnknownMember -> badRequest("membre inconnu")
            EntryWriteResult.NotFound -> notFound()
        }
    }

    @PUT
    @Path("/entries/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = FinanceEntryDto::class, responseCode = "200")
    suspend fun updateEntry(@PathParam("id") id: UUID, request: FinanceEntryRequest): Response {
        val input = request.toInput() ?: return badRequest(entryValidationMessage(request))
        return when (val result = updateFinanceEntryUseCase(id, input)) {
            is EntryWriteResult.Ok -> Response.ok(result.entity.toDto()).build()
            EntryWriteResult.UnknownMember -> badRequest("membre inconnu")
            EntryWriteResult.NotFound -> notFound()
        }
    }

    @DELETE
    @Path("/entries/{id}")
    suspend fun deleteEntry(@PathParam("id") id: UUID): Response =
        when (deleteFinanceEntryUseCase(id)) {
            EntryDeleteResult.Deleted -> Response.noContent().build()
            EntryDeleteResult.NotFound -> notFound()
            EntryDeleteResult.RecurringForbidden ->
                conflict("une écriture générée par une règle récurrente ne peut pas être supprimée : modifiez-la ou désactivez la règle")
        }

    @GET
    @Path("/rules")
    suspend fun listRules(): List<RecurringRuleDto> = listRecurringRulesUseCase().map { it.toDto() }

    @POST
    @Path("/rules")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = RecurringRuleDto::class, responseCode = "201")
    suspend fun createRule(request: RecurringRuleRequest): Response {
        val input = request.toInput() ?: return badRequest(ruleValidationMessage(request))
        return when (val result = createRecurringRuleUseCase(input)) {
            is RuleWriteResult.Ok -> Response.status(Response.Status.CREATED).entity(result.entity.toDto()).build()
            RuleWriteResult.UnknownMember -> badRequest("membre inconnu")
            RuleWriteResult.NotFound -> notFound()
        }
    }

    @PUT
    @Path("/rules/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = RecurringRuleDto::class, responseCode = "200")
    suspend fun updateRule(@PathParam("id") id: UUID, request: RecurringRuleRequest): Response {
        val input = request.toInput() ?: return badRequest(ruleValidationMessage(request))
        return when (val result = updateRecurringRuleUseCase(id, input)) {
            is RuleWriteResult.Ok -> Response.ok(result.entity.toDto()).build()
            RuleWriteResult.UnknownMember -> badRequest("membre inconnu")
            RuleWriteResult.NotFound -> notFound()
        }
    }

    @DELETE
    @Path("/rules/{id}")
    suspend fun deleteRule(@PathParam("id") id: UUID): Response =
        if (deleteRecurringRuleUseCase(id)) Response.noContent().build() else notFound()

    @GET
    @Path("/settings")
    suspend fun getSettings(): FinanceSettingsDto = getFinanceSettingsUseCase().toDto()

    @PUT
    @Path("/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponseSchema(value = FinanceSettingsDto::class, responseCode = "200")
    suspend fun updateSettings(request: FinanceSettingsRequest): Response {
        val price = request.kwhPrice
        if (price != null && price <= BigDecimal.ZERO) return badRequest("le prix du kWh doit être positif")
        return Response.ok(updateFinanceSettingsUseCase(price).toDto()).build()
    }

    private fun FinanceEntryRequest.toInput(): FinanceEntryInput? {
        val type = type ?: return null
        val label = label?.takeIf { it.isNotBlank() } ?: return null
        val amountCents = amountCents?.takeIf { it > 0 } ?: return null
        val entryDate = entryDate ?: return null
        if (type == EntryType.CONTRIBUTION && memberId == null) return null
        return FinanceEntryInput(
            type = type,
            label = label,
            vendor = vendor?.takeIf { it.isNotBlank() },
            amountCents = amountCents,
            entryDate = entryDate,
            memberId = memberId,
            notes = notes?.takeIf { it.isNotBlank() },
        )
    }

    private fun entryValidationMessage(request: FinanceEntryRequest): String = when {
        request.type == null -> "le type est requis (CONTRIBUTION ou EXPENSE)"
        request.label.isNullOrBlank() -> "le libellé est requis"
        request.amountCents == null || request.amountCents <= 0 -> "le montant doit être positif"
        request.entryDate == null -> "la date est requise"
        request.type == EntryType.CONTRIBUTION && request.memberId == null -> "une cotisation doit référencer un membre"
        else -> "requête invalide"
    }

    private fun RecurringRuleRequest.toInput(): RecurringRuleInput? {
        val type = type ?: return null
        val label = label?.takeIf { it.isNotBlank() } ?: return null
        val amountCents = amountCents?.takeIf { it > 0 } ?: return null
        val dayOfMonth = dayOfMonth?.takeIf { it in 1..28 } ?: return null
        val startDate = startDate ?: return null
        if (type == EntryType.CONTRIBUTION && memberId == null) return null
        if (endDate != null && endDate.isBefore(startDate)) return null
        return RecurringRuleInput(
            type = type,
            label = label,
            amountCents = amountCents,
            dayOfMonth = dayOfMonth,
            memberId = memberId,
            vendor = vendor?.takeIf { it.isNotBlank() },
            active = active ?: true,
            startDate = startDate,
            endDate = endDate,
        )
    }

    private fun ruleValidationMessage(request: RecurringRuleRequest): String = when {
        request.type == null -> "le type est requis (CONTRIBUTION ou EXPENSE)"
        request.label.isNullOrBlank() -> "le libellé est requis"
        request.amountCents == null || request.amountCents <= 0 -> "le montant doit être positif"
        request.dayOfMonth == null || request.dayOfMonth !in 1..28 -> "le jour du mois doit être entre 1 et 28"
        request.startDate == null -> "la date de début est requise"
        request.type == EntryType.CONTRIBUTION && request.memberId == null -> "une cotisation récurrente doit référencer un membre"
        request.endDate != null && request.endDate.isBefore(request.startDate) ->
            "la date de fin doit être postérieure à la date de début"
        else -> "requête invalide"
    }
}
