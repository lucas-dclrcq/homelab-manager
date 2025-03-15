package org.hoohoot.homelab.manager.application.ports.arr.sonarr

import java.time.ZonedDateTime

interface SonarrGateway {
    suspend fun getSeriesCalendar(start: ZonedDateTime, end: ZonedDateTime): List<Episode>
}

