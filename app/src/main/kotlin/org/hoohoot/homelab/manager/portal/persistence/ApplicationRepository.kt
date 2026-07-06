package org.hoohoot.homelab.manager.portal.persistence

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

data class ApplicationSummary(
    val id: UUID,
    val name: String,
    val category: String,
    val description: String,
    val url: String,
    val requiresVpn: Boolean,
    val hasLogo: Boolean,
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
                           case when a.logo is null then false else true end)
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

    suspend fun findLogo(id: UUID): ApplicationLogo? {
        val entity = Panache.withSession {
            ApplicationEntity.findById(id)
        }.awaitSuspending()
        val content = entity?.logo ?: return null
        return ApplicationLogo(content, entity.logoContentType ?: "application/octet-stream")
    }
}
