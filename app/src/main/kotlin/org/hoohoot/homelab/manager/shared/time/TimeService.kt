package org.hoohoot.homelab.manager.shared.time

import jakarta.enterprise.context.ApplicationScoped
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.time.Instant
import kotlin.time.Clock

data class Week(val start: Instant, val end: Instant)

class FixedClock(private val fixedInstant: Instant) : Clock {
    override fun now(): Instant = fixedInstant
}

@ApplicationScoped
class TimeService {
    private var clock: Clock = Clock.System

    fun getCurrentWeek(): Week {
        val now = clock.now()
        val utcNow = java.time.Instant.ofEpochMilli(now.toEpochMilliseconds())
        val monday = utcNow.atZone(ZoneOffset.UTC).toLocalDate()
            .with(DayOfWeek.MONDAY)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        val end = monday.plus(Duration.ofDays(7)).minusMillis(1)

        return Week(
            Instant.fromEpochMilliseconds(monday.toEpochMilli()),
            Instant.fromEpochMilliseconds(end.toEpochMilli()),
        )
    }

    fun getNextWeek(): Week {
        val now = clock.now()
        val utcNow = java.time.Instant.ofEpochMilli(now.toEpochMilliseconds())
        val nextMonday = utcNow.atZone(ZoneOffset.UTC).toLocalDate()
            .with(DayOfWeek.MONDAY)
            .plusWeeks(1)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
        val end = nextMonday.plus(Duration.ofDays(7)).minusMillis(1)

        return Week(
            Instant.fromEpochMilliseconds(nextMonday.toEpochMilli()),
            Instant.fromEpochMilliseconds(end.toEpochMilli()),
        )
    }

    fun getDaysSince(date: LocalDate): Int {
        val currentDate = java.time.Instant.ofEpochMilli(clock.now().toEpochMilliseconds())
            .atZone(ZoneOffset.UTC).toLocalDate()
        return date.daysUntil(currentDate)
    }

    fun setFixedClock(fixedInstant: Instant) {
        this.clock = FixedClock(fixedInstant)
    }
}
