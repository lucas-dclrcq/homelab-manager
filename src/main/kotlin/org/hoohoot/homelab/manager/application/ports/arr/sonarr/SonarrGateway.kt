package org.hoohoot.homelab.manager.application.ports.arr.sonarr

import kotlinx.datetime.Instant

interface SonarrGateway {
    suspend fun getSeriesCalendar(start: Instant, end: Instant): List<Episode>
}
