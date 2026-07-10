package org.hoohoot.homelab.manager.problems.infra

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.problems.domain.LibrarySeries
import org.hoohoot.homelab.manager.problems.domain.ports.SeriesLibrary
import org.hoohoot.homelab.manager.shared.arr.sonarr.Series
import org.hoohoot.homelab.manager.shared.arr.sonarr.SonarrRestClient

@ApplicationScoped
class SonarrProblemsAdapter(
    @param:RestClient private val sonarrRestClient: SonarrRestClient,
) : SeriesLibrary {

    override suspend fun allSeries(): List<LibrarySeries> =
        sonarrRestClient.getSeries().orEmpty().mapNotNull { it.toLibrarySeries() }

    private fun Series.toLibrarySeries(): LibrarySeries? {
        val seriesId = id ?: return null
        val seriesTitle = title ?: return null
        return LibrarySeries(
            sonarrSeriesId = seriesId,
            title = seriesTitle,
            year = year,
            posterUrl = images.orEmpty().firstOrNull { it.coverType == "poster" }?.remoteUrl,
            overview = overview,
        )
    }
}
