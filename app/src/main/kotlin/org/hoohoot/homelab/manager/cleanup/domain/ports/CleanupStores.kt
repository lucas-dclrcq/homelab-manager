package org.hoohoot.homelab.manager.cleanup.domain.ports

import org.hoohoot.homelab.manager.cleanup.domain.ActiveProblemIds
import org.hoohoot.homelab.manager.cleanup.domain.CleanupConfig
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCampaignEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupCandidateEntity
import org.hoohoot.homelab.manager.cleanup.infra.CleanupProtectionEntity
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

interface Protections {
    suspend fun all(): List<CleanupProtectionEntity>
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
