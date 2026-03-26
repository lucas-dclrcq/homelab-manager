package org.hoohoot.homelab.manager.time

import jakarta.enterprise.context.ApplicationScoped
import kotlinx.datetime.*
import kotlin.time.Duration

data class Week(val start: Instant, val end: Instant)

class FixedClock(private val fixedInstant: Instant): Clock {
    override fun now(): Instant  = fixedInstant
}

@ApplicationScoped
class TimeService {
    private var clock: Clock = Clock.System

    fun getCurrentWeek(): Week {
        val now = clock.now().toLocalDateTime(TimeZone.UTC)
            .date
            .let { it.minus(it.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY) }
            .atStartOfDayIn(TimeZone.UTC)

        val end = now.plus(Duration.parse("P6D"))
            .plus(23, DateTimeUnit.HOUR)
            .plus(59, DateTimeUnit.MINUTE)

        return Week(now, end)
    }

    fun getNextWeek(): Week {
        val nextMonday = clock.now().toLocalDateTime(TimeZone.UTC)
            .date
            .let { it.plus(7 - it.dayOfWeek.ordinal.toLong(), DateTimeUnit.DAY) }
            .atStartOfDayIn(TimeZone.UTC)

        val end = nextMonday.plus(Duration.parse("P6D"))
            .plus(23, DateTimeUnit.HOUR)
            .plus(59, DateTimeUnit.MINUTE)

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
