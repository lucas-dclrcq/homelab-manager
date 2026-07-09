package org.hoohoot.homelab.manager.members.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.members.domain.ports.DirectoryUser
import org.hoohoot.homelab.manager.members.domain.ports.MemberSyncStats
import org.hoohoot.homelab.manager.members.domain.ports.Members
import java.time.LocalDateTime
import java.util.UUID

@ApplicationScoped
class MemberRepository : Members {

    override suspend fun listAll(): List<MemberEntity> =
        Panache.withSession {
            MemberEntity.listAll()
        }.awaitSuspending().sortedBy { it.displayName.lowercase() }

    override suspend fun findById(id: UUID): MemberEntity? =
        Panache.withSession {
            MemberEntity.findById(id)
        }.awaitSuspending()

    override suspend fun syncFromDirectory(users: List<DirectoryUser>): MemberSyncStats =
        Panache.withTransaction {
            MemberEntity.list("authentikPk is not null").flatMap { existing ->
                val byPk = existing.associateBy { it.authentikPk!! }
                val now = LocalDateTime.now()
                var updated = 0
                var deactivated = 0

                val toCreate = users.filter { it.pk !in byPk }.map { user ->
                    MemberEntity().apply {
                        id = UUID.randomUUID()
                        authentikPk = user.pk
                        username = user.username
                        displayName = user.displayName
                        email = user.email
                        active = user.active
                        createdAt = now
                    }
                }

                users.forEach { user ->
                    byPk[user.pk]?.let { entity ->
                        entity.username = user.username
                        entity.displayName = user.displayName
                        entity.email = user.email
                        entity.active = user.active
                        entity.updatedAt = now
                        updated++
                    }
                }

                val knownPks = users.map { it.pk }.toSet()
                existing.filter { it.authentikPk !in knownPks && it.active }.forEach { entity ->
                    entity.active = false
                    entity.updatedAt = now
                    deactivated++
                }

                val stats = MemberSyncStats(created = toCreate.size, updated = updated, deactivated = deactivated)
                if (toCreate.isEmpty()) {
                    Uni.createFrom().item(stats)
                } else {
                    MemberEntity.persist(toCreate).map { stats }
                }
            }
        }.awaitSuspending()
}
