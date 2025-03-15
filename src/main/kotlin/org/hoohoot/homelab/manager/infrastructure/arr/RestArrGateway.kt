package org.hoohoot.homelab.manager.infrastructure.arr

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.Episode
import org.hoohoot.homelab.manager.application.ports.arr.sonarr.SonarrGateway

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ApplicationScoped
class RestArrGateway(@param:RestClient private val sonarrRestClient: SonarrRestClient) : SonarrGateway {
    override suspend fun getSeriesCalendar(start: ZonedDateTime, end: ZonedDateTime): List<Episode> =
        this.sonarrRestClient.getCalendar(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(start), DateTimeFormatter.ISO_ZONED_DATE_TIME.format(end), true) ?: emptyList()
}