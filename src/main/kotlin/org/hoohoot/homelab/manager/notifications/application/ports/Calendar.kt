package org.hoohoot.homelab.manager.notifications.application.ports

import java.time.ZonedDateTime

data class Week(val start: ZonedDateTime, val end: ZonedDateTime)

interface Calendar {
    fun getCurrentWeek(): Week
}