package org.hoohoot.homelab.manager.finances.domain.ports

import org.hoohoot.homelab.manager.finances.infra.RecurringRuleEntity
import java.util.UUID

interface RecurringRules {
    suspend fun listAll(): List<RecurringRuleEntity>
    suspend fun listActive(): List<RecurringRuleEntity>
    suspend fun findById(id: UUID): RecurringRuleEntity?
    suspend fun save(entity: RecurringRuleEntity): RecurringRuleEntity
    suspend fun update(id: UUID, mutate: (RecurringRuleEntity) -> Unit): RecurringRuleEntity?
    suspend fun delete(id: UUID): Boolean
}
