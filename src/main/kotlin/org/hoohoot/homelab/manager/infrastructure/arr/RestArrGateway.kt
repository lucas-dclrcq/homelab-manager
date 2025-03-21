package org.hoohoot.homelab.manager.infrastructure.arr

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Episode
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.SonarrGateway

@ApplicationScoped
class RestArrGateway(@param:RestClient private val sonarrRestClient: SonarrRestClient) : SonarrGateway {
    override suspend fun getSeriesCalendar(start: Instant, end: Instant): List<Episode> =
        this.sonarrRestClient
            .getCalendar(start.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET), end.format(DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET), true)
            ?: emptyList()
}