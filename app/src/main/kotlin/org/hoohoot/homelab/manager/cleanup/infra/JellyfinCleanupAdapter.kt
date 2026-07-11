package org.hoohoot.homelab.manager.cleanup.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.cleanup.domain.JellyfinEntryType
import org.hoohoot.homelab.manager.cleanup.domain.JellyfinLibraryEntry
import org.hoohoot.homelab.manager.cleanup.domain.ports.JellyfinCatalog
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinLibraryItem
import org.hoohoot.homelab.manager.shared.jellyfin.JellyfinRestClient

@ApplicationScoped
class JellyfinCleanupAdapter(
    @param:RestClient private val jellyfinRestClient: JellyfinRestClient,
) : JellyfinCatalog {
    companion object {
        private const val FIELDS = "ProviderIds,ProductionYear"
    }

    override suspend fun libraryEntries(): List<JellyfinLibraryEntry> =
        fetch("Movie", JellyfinEntryType.MOVIE) + fetch("Series", JellyfinEntryType.SERIES)

    private suspend fun fetch(itemType: String, entryType: JellyfinEntryType): List<JellyfinLibraryEntry> =
        jellyfinRestClient.getItems(includeItemTypes = itemType, recursive = true, fields = FIELDS)
            .items.orEmpty()
            .mapNotNull { it.toEntry(entryType) }

    private fun JellyfinLibraryItem.toEntry(entryType: JellyfinEntryType): JellyfinLibraryEntry? {
        val itemId = id ?: return null
        val itemName = name ?: return null
        return JellyfinLibraryEntry(
            itemId = itemId,
            name = itemName,
            productionYear = productionYear,
            type = entryType,
            tmdbId = providerId("Tmdb"),
            imdbId = providerId("Imdb"),
            tvdbId = providerId("Tvdb"),
        )
    }

    // Jellyfin n'est pas constant sur la casse des clés ProviderIds selon les versions
    private fun JellyfinLibraryItem.providerId(key: String): String? =
        providerIds?.entries?.firstOrNull { it.key.equals(key, ignoreCase = true) }?.value?.ifBlank { null }
}
