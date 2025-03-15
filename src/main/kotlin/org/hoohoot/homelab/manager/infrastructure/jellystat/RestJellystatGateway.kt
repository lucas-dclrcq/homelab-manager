package org.hoohoot.homelab.manager.infrastructure.jellystat

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.JellystatGateway
import org.hoohoot.homelab.manager.application.ports.JellystatMediaType
import org.hoohoot.homelab.manager.application.ports.UniqueViewerStatistics

@ApplicationScoped
class RestJellystatGateway(@RestClient private val jellystatRestClient: JellystatRestClient) : JellystatGateway {
    override suspend fun getMostPopularByType(numberOfDays: Int, type: JellystatMediaType): List<UniqueViewerStatistics> =
        jellystatRestClient.getMostPopularByType(StatisticsRequest(numberOfDays.toString(), type.name))
            .map { UniqueViewerStatistics(it.uniqueViewers?.toInt() ?: 0, it.name ?: "unknown") }
}