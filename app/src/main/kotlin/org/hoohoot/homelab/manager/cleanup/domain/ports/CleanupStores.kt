package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.ActiveProblemIds
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupSuggestionEntity
import java.time.LocalDateTime
import java.util.UUID

interface Campaigns {
    suspend fun findActive(): CleanupCampaignEntity?
    suspend fun find(id: UUID): CleanupCampaignEntity?
    suspend fun save(entity: CleanupCampaignEntity): CleanupCampaignEntity
    suspend fun update(id: UUID, mutate: (CleanupCampaignEntity) -> Unit): CleanupCampaignEntity?
    suspend fun listDue(now: LocalDateTime): List<CleanupCampaignEntity>
    suspend fun listAll(): List<CleanupCampaignEntity>
}

interface Candidates {
    suspend fun saveAll(entities: List<CleanupCandidateEntity>)
    suspend fun listByCampaign(campaignId: UUID): List<CleanupCandidateEntity>
    suspend fun find(id: UUID): CleanupCandidateEntity?
    suspend fun update(id: UUID, mutate: (CleanupCandidateEntity) -> Unit): CleanupCandidateEntity?
}

interface Suggestions {
    suspend fun listPending(): List<CleanupSuggestionEntity>

    // Suggestions en attente + issues récentes, pour l'affichage
    suspend fun listRecent(resolvedSince: LocalDateTime): List<CleanupSuggestionEntity>
    suspend fun listDue(now: LocalDateTime): List<CleanupSuggestionEntity>
    suspend fun find(id: UUID): CleanupSuggestionEntity?
    suspend fun findPendingByAnnouncementEvent(eventId: String): CleanupSuggestionEntity?
    suspend fun save(entity: CleanupSuggestionEntity): CleanupSuggestionEntity
    suspend fun update(id: UUID, mutate: (CleanupSuggestionEntity) -> Unit): CleanupSuggestionEntity?
}

data class ProtectionsPage(
    val items: List<CleanupProtectionEntity>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
)

interface Protections {
    suspend fun all(): List<CleanupProtectionEntity>

    // Vue paginée pour l'UI ; les vérifications de couverture passent par all()
    suspend fun page(page: Int, pageSize: Int): ProtectionsPage
    suspend fun find(id: UUID): CleanupProtectionEntity?
    suspend fun save(entity: CleanupProtectionEntity): CleanupProtectionEntity
    suspend fun delete(id: UUID): Boolean
}

interface ActiveProblems {
    // Médias avec un problem_workflow non terminal : jamais candidats à la suppression
    suspend fun activeMediaIds(): ActiveProblemIds
}

interface CleanupConfigStore {
    fun effective(): CleanupConfig
}
