package org.hoohoot.homelab.manager.portal.resource

import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadEntity
import org.hoohoot.homelab.manager.portal.persistence.MediaDownloadRepository
import java.time.LocalDateTime

data class TimelineEventDto(
    val id: Long,
    val eventType: String,
    val title: String,
    val details: Map<String, String>,
    val occurredAt: LocalDateTime,
)

data class TimelinePageDto(
    val items: List<TimelineEventDto>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalCount: Long,
)

@Path("/api/timeline")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Portal")
class TimelineResource(private val mediaDownloadRepository: MediaDownloadRepository) {

    @GET
    suspend fun getTimeline(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("pageSize") @DefaultValue("20") pageSize: Int,
    ): TimelinePageDto {
        val sanitizedPage = page.coerceAtLeast(0)
        val sanitizedPageSize = pageSize.coerceIn(1, 100)
        val downloadPage = mediaDownloadRepository.findPage(sanitizedPage, sanitizedPageSize)
        val totalPages = ((downloadPage.totalCount + sanitizedPageSize - 1) / sanitizedPageSize).toInt()
        return TimelinePageDto(
            items = downloadPage.items.map { it.toTimelineEvent() },
            page = sanitizedPage,
            pageSize = sanitizedPageSize,
            totalPages = totalPages,
            totalCount = downloadPage.totalCount,
        )
    }

    private fun MediaDownloadEntity.toTimelineEvent() = TimelineEventDto(
        id = id!!,
        eventType = when (mediaType) {
            MediaDownloadEntity.MEDIA_TYPE_MOVIE -> "movie_downloaded"
            MediaDownloadEntity.MEDIA_TYPE_EPISODE -> "episode_downloaded"
            MediaDownloadEntity.MEDIA_TYPE_ALBUM -> "album_downloaded"
            else -> "subtitles_downloaded"
        },
        title = title,
        details = buildMap {
            quality?.let { put("quality", it) }
            seasonNumber?.let { put("seasonNumber", it.toString()) }
            episodeNumber?.let { put("episodeNumber", it.toString()) }
            episodeTitle?.let { put("episodeTitle", it) }
            language?.let { put("language", it) }
            provider?.let { put("provider", it) }
            artist?.let { put("artist", it) }
        },
        occurredAt = downloadedAt,
    )
}
