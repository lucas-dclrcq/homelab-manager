package org.hoohoot.homelab.manager.notifications.infrastructure.time

import jakarta.enterprise.context.ApplicationScoped
import org.hoohoot.homelab.manager.notifications.application.ports.Calendar
import org.hoohoot.homelab.manager.notifications.application.ports.Week
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@ApplicationScoped
class TimeService : Calendar {
    private var clock = Clock.systemUTC()

    override fun getCurrentWeek(): Week {
        val now = LocalDateTime.now(clock)
            .with(java.time.DayOfWeek.MONDAY)
            .toLocalDate()
            .atStartOfDay(java.time.ZoneOffset.UTC)

        val end = now.plusDays(6)
            .withHour(23)
            .withMinute(59)

        return Week(now, end)
    }

    fun setFixedClock(fixedInstant: Instant, zone: ZoneId) {
        this.clock = Clock.fixed(fixedInstant, zone)
    }
}
