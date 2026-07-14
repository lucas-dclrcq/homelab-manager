package org.hoohoot.homelab.manager.shared.time

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.*

data class Week(val start: Instant, val end: Instant)

class FixedClock(private val fixedInstant: Instant): Clock {
    override fun now(): Instant  = fixedInstant
}

private fun kotlin.time.Instant.toKotlinxInstant(): Instant =
    Instant.fromEpochMilliseconds(this.toEpochMilliseconds())

@ApplicationScoped
class TimeService {
    private var clock: Clock = Clock.System

    fun getCurrentWeek(): Week {
        val now = clock.now().toLocalDateTime(TimeZone.UTC)
            .date
            .let { it.minus(it.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY) }
            .atStartOfDayIn(TimeZone.UTC)
            .toKotlinxInstant()

        val end = now.plus(DateTimePeriod(days = 6, hours = 23, minutes = 59), TimeZone.UTC)

        return Week(now, end)
    }

    fun getNextWeek(): Week {
        val nextMonday = clock.now().toLocalDateTime(TimeZone.UTC)
            .date
            .let { it.plus(7 - it.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY) }
            .atStartOfDayIn(TimeZone.UTC)
            .toKotlinxInstant()

        val end = nextMonday.plus(DateTimePeriod(days = 6, hours = 23, minutes = 59), TimeZone.UTC)

        return Week(nextMonday, end)
    }

    fun getDaysSince(date: LocalDate): Int {
        val currentDate = clock.now().toLocalDateTime(TimeZone.UTC).date
        return date.daysUntil(currentDate)
    }

    fun setFixedClock(fixedInstant: Instant) {
        this.clock = FixedClock(fixedInstant)
    }
}
