package org.hoohoot.homelab.manager.notifications.infrastructure.arr

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.rest.client.inject.RestClient
import org.hoohoot.homelab.manager.notifications.application.ports.arr.ArrGateway
import org.hoohoot.homelab.manager.notifications.application.ports.arr.Episode
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@ApplicationScoped
class RestArrGateway(@param:RestClient private val sonarrRestClient: SonarrRestClient) : ArrGateway {
    override suspend fun getSeriesCalendar(start: ZonedDateTime, end: ZonedDateTime): List<Episode> =
        this.sonarrRestClient.getCalendar(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(start), DateTimeFormatter.ISO_ZONED_DATE_TIME.format(end), true) ?: emptyList()
}