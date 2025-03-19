package org.hoohoot.homelab.manager.infrastructure.jellystat

import io.quarkus.logging.Log
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.*
import kotlin.time.Duration

@ApplicationScoped
class RestJellystatGateway(@RestClient private val jellystatRestClient: JellystatRestClient) : JellystatGateway {
    override suspend fun getMostPopularByType(
        lastNumberOfDays: Int,
        type: JellystatMediaType
    ): List<UniqueViewerStatistics> =
        jellystatRestClient.getMostPopularByType(StatisticsRequest(lastNumberOfDays.toString(), type.name))
            .map { UniqueViewerStatistics(it.uniqueViewers?.toInt() ?: 0, it.name ?: "unknown") }

    override suspend fun getMostViewedByType(lastNumberOfDays: Int, type: JellystatMediaType): List<PlaysStatistics> =
        jellystatRestClient.getMostViewedByType(StatisticsRequest(lastNumberOfDays.toString(), type.name))
            .map {
                PlaysStatistics(
                    it.name ?: "unknown",
                    it.plays?.toInt() ?: 0,
                    it.totalPlaybackDuration.let { duration -> Duration.parse("${duration}s")
            })
}

override suspend fun getMediaWatchEvents(itemId: String): List<WatchEvent> {
    val watchEvents = mutableListOf<WatchEvent>();

    var page = 1L;
    var totalPages = 1L;

    do {
        Log.info("Getting watch events for item $itemId, page $page of $totalPages")
        val (currentPage, pages, _, _, _, results) = jellystatRestClient.getItemHistory(ItemIdRequest(itemId), page, 50)

        watchEvents.addAll(
            results
                .filter { it.userName != null && it.episodeNumber != null && it.seasonNumber != null && it.fullName != null }
                .map {
                    WatchEvent(
                        it.userName!!,
                        it.episodeNumber!!.toInt(),
                        it.seasonNumber!!.toInt(),
                        it.fullName!!
                    )
                })

        page = currentPage + 1
        totalPages = pages

    } while (page <= totalPages)

    return watchEvents.toList()
}
}