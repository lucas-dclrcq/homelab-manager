package org.hoohoot.homelab.manager.portal

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.portal.persistence.HomelabEventEntity
import org.hoohoot.homelab.manager.portal.persistence.HomelabEventRepository
import java.time.LocalDateTime

object HomelabEventTypes {
    const val MOVIE_DOWNLOADED = "movie_downloaded"
    const val EPISODE_DOWNLOADED = "episode_downloaded"
    const val SUBTITLES_DOWNLOADED = "subtitles_downloaded"
}

@ApplicationScoped
class HomelabEventRecorder(private val repository: HomelabEventRepository) {

    // Timeline recording must never fail the notification flow
    suspend fun record(eventType: String, title: String, details: Map<String, String?>) {
        try {
            val entity = HomelabEventEntity()
            entity.eventType = eventType
            entity.title = title
            entity.details = details.filterValues { !it.isNullOrBlank() }.mapValues { it.value!! }
            entity.occurredAt = LocalDateTime.now()
            repository.save(entity)
        } catch (exception: Exception) {
            Log.error("Failed to record homelab event '$eventType' for '$title'", exception)
        }
    }
}
