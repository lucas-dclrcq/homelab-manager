package org.hoohoot.homelab.manager.finances.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.finances.domain.ports.RecurringRules
import java.util.UUID

@ApplicationScoped
class RecurringRuleRepository : RecurringRules {

    override suspend fun listAll(): List<RecurringRuleEntity> =
        Panache.withSession {
            RecurringRuleEntity.listAll()
        }.awaitSuspending().sortedBy { it.label.lowercase() }

    override suspend fun listActive(): List<RecurringRuleEntity> =
        Panache.withSession {
            RecurringRuleEntity.list("active = true")
        }.awaitSuspending()

    override suspend fun findById(id: UUID): RecurringRuleEntity? =
        Panache.withSession {
            RecurringRuleEntity.findById(id)
        }.awaitSuspending()

    override suspend fun save(entity: RecurringRuleEntity): RecurringRuleEntity =
        Panache.withTransaction {
            entity.persist<RecurringRuleEntity>()
        }.awaitSuspending()

    override suspend fun update(id: UUID, mutate: (RecurringRuleEntity) -> Unit): RecurringRuleEntity? =
        Panache.withTransaction {
            RecurringRuleEntity.findById(id).invoke { entity -> entity?.let(mutate) }
        }.awaitSuspending()

    override suspend fun delete(id: UUID): Boolean =
        Panache.withTransaction {
            RecurringRuleEntity.deleteById(id)
        }.awaitSuspending()
}
