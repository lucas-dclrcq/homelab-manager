package org.hoohoot.homelab.manager.library.infra

import io.quarkus.hibernate.reactive.panache.kotlin.Panache
import io.quarkus.panache.common.Page
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDateTime

data class MediaDownloadPage(
    val items: List<MediaDownloadEntity>,
    val totalCount: Long,
)

@ApplicationScoped
class MediaDownloadRepository {

    suspend fun findPage(page: Int, pageSize: Int): MediaDownloadPage {
        val items = Panache.withSession {
            MediaDownloadEntity.findAll(Sort.descending("downloadedAt", "id"))
                .page(Page.of(page, pageSize))
                .list()
        }.awaitSuspending()
        val totalCount = Panache.withSession {
            MediaDownloadEntity.count()
        }.awaitSuspending()
        return MediaDownloadPage(items, totalCount)
    }

    suspend fun latestDownloadedAt(source: String): LocalDateTime? {
        return Panache.withSession {
            MediaDownloadEntity.find("source = ?1", Sort.descending("downloadedAt"), source)
                .firstResult()
        }.awaitSuspending()?.downloadedAt
    }

    // Insère uniquement les candidats absents (dédup sur source + externalId), renvoie le nombre inséré
    suspend fun saveNewDownloads(source: String, candidates: List<MediaDownloadEntity>): Int {
        if (candidates.isEmpty()) return 0
        return Panache.withTransaction {
            MediaDownloadEntity.find(
                "source = ?1 and externalId in ?2",
                source,
                candidates.map { it.externalId },
            ).list().chain { existing ->
                val existingIds = existing.map { it.externalId }.toSet()
                val newEntities = candidates
                    .distinctBy { it.externalId }
                    .filter { it.externalId !in existingIds }
                if (newEntities.isEmpty()) {
                    Uni.createFrom().item(0)
                } else {
                    MediaDownloadEntity.persist(newEntities).map { newEntities.size }
                }
            }
        }.awaitSuspending()
    }
}
