package org.hoohoot.homelab.manager.infrastructure.jellyfin

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.JellyfinGateway
import org.hoohoot.homelab.manager.application.ports.MediaSearchResult

@ApplicationScoped
class RestJellyfinGateway(@RestClient private val jellyfinRestClient: JellyfinRestClient) : JellyfinGateway {
    override suspend fun searchSeries(searchTerm: String): List<MediaSearchResult> {
        return this.jellyfinRestClient.getSearchHintResult(searchTerm, "Series")
            .searchHints
            ?.map { MediaSearchResult(it.itemId ?: "", it.name ?: "", it.type ?: "") }
            ?: emptyList()
    }
}