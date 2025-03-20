package org.hoohoot.homelab.manager.infrastructure.time

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.*
import org.hoohoot.homelab.manager.application.ports.Calendar
import org.hoohoot.homelab.manager.application.ports.Week
import kotlin.time.Duration


class FixedClock(private val fixedInstant: Instant): Clock {
    override fun now(): Instant  = fixedInstant
}

@ApplicationScoped
class TimeService : Calendar {
    private var clock: Clock = Clock.System

    override fun getCurrentWeek(): Week {
        val now = clock.now().toLocalDateTime(TimeZone.UTC)
            .date
            .let { it.minus(it.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY) }
            .atStartOfDayIn(TimeZone.UTC)

        val end = now.plus(Duration.parse("P6D"))
            .plus(23, DateTimeUnit.HOUR)
            .plus(59, DateTimeUnit.MINUTE)

        return Week(now, end)
    }

    override fun getDaysSince(date: LocalDate): Int {
        val currentDate = clock.now().toLocalDateTime(TimeZone.UTC).date
        return date.daysUntil(currentDate)
    }

    fun setFixedClock(fixedInstant: Instant) {
        this.clock = FixedClock(fixedInstant)
    }
}
