package org.hoohoot.homelab.manager.notifications.application.ports.arr

import java.time.LocalDateTime
import java.time.ZonedDateTime

interface ArrGateway {
    suspend fun getSeriesCalendar(start: ZonedDateTime, end: ZonedDateTime): List<Episode>
}

