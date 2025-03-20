package org.hoohoot.homelab.manager.application.ports

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate


data class Week(val start: Instant, val end: Instant)

interface Calendar {
    fun getCurrentWeek(): Week
    fun getDaysSince(date: LocalDate): Int
}