package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime
import java.util.UUID

data class ApplicationSummary(
    val id: UUID,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val hasLogo: Boolean,
    val managedBy: String?,
    val externalId: String?,
    val updatedAt: LocalDateTime?,
)

data class ApplicationLogo(val content: ByteArray, val contentType: String)

@ApplicationScoped
class ApplicationRepository {

    suspend fun listSummaries(): List<ApplicationSummary> =
        Panache.withSession {
            Panache.getSession().flatMap { session ->
                session.createQuery(
                    """select new org.hoohoot.homelab.manager.portal.persistence.ApplicationSummary(
                           a.id, a.name, a.category, a.description, a.url, a.requiresVpn,
                           case when a.logo is null then false else true end, a.managedBy, a.externalId, a.updatedAt)
                       from ApplicationEntity a
                       order by a.category, a.name""",
                    ApplicationSummary::class.java
                ).resultList
            }
        }.awaitSuspending()

    suspend fun save(entity: ApplicationEntity): ApplicationEntity =
        Panache.withTransaction {
            entity.persist<ApplicationEntity>()
        }.awaitSuspending()

    suspend fun update(id: UUID, mutate: (ApplicationEntity) -> Unit): ApplicationEntity? =
        Panache.withTransaction {
            ApplicationEntity.findById(id).invoke { entity -> entity?.let(mutate) }
        }.awaitSuspending()

    suspend fun delete(id: UUID): Boolean =
        Panache.withTransaction {
            ApplicationEntity.deleteById(id)
        }.awaitSuspending()

    suspend fun findLogo(id: UUID): ApplicationLogo? {
        val entity = Panache.withSession {
            ApplicationEntity.findById(id)
        }.awaitSuspending()
        val content = entity?.logo ?: return null
        return ApplicationLogo(content, entity.logoContentType ?: "application/octet-stream")
    }
}
