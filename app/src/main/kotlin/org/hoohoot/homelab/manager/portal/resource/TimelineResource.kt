package org.hoohoot.homelab.manager.portal.resource

import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.hoohoot.homelab.manager.portal.persistence.HomelabEventRepository
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
class TimelineResource(private val eventRepository: HomelabEventRepository) {

    @GET
    suspend fun getTimeline(
        @QueryParam("page") @DefaultValue("0") page: Int,
        @QueryParam("pageSize") @DefaultValue("20") pageSize: Int,
    ): TimelinePageDto {
        val sanitizedPage = page.coerceAtLeast(0)
        val sanitizedPageSize = pageSize.coerceIn(1, 100)
        val eventPage = eventRepository.findPage(sanitizedPage, sanitizedPageSize)
        val totalPages = ((eventPage.totalCount + sanitizedPageSize - 1) / sanitizedPageSize).toInt()
        return TimelinePageDto(
            items = eventPage.items.map {
                TimelineEventDto(
                    id = it.id!!,
                    eventType = it.eventType,
                    title = it.title,
                    details = it.details.orEmpty(),
                    occurredAt = it.occurredAt,
                )
            },
            page = sanitizedPage,
            pageSize = sanitizedPageSize,
            totalPages = totalPages,
            totalCount = eventPage.totalCount,
        )
    }
}
