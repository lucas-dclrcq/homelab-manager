package org.hoohoot.homelab.manager.statistics.domain

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

data class StatsRange(
    val fromUtc: LocalDateTime,
    val toUtc: LocalDateTime,
    val fromLocal: LocalDateTime,
    val toLocal: LocalDateTime,
    val allTime: Boolean,
)

@ApplicationScoped
class PeriodResolver(@ConfigProperty(name = "statistics.timezone") timezoneId: String) {
    val zone: ZoneId = ZoneId.of(timezoneId)

    fun resolve(period: StatsPeriod, now: Instant = Instant.now()): StatsRange {
        val nowLocal = now.atZone(zone)
        val (start, end) = when (period) {
            StatsPeriod.TODAY -> {
                val start = nowLocal.toLocalDate().atStartOfDay(zone)
                start to start.plusDays(1)
            }
            StatsPeriod.THIS_WEEK -> {
                val start = nowLocal.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay(zone)
                start to start.plusWeeks(1)
            }
            StatsPeriod.THIS_MONTH -> {
                val start = nowLocal.toLocalDate().withDayOfMonth(1).atStartOfDay(zone)
                start to start.plusMonths(1)
            }
            StatsPeriod.THIS_YEAR -> {
                val start = nowLocal.toLocalDate().withDayOfYear(1).atStartOfDay(zone)
                start to start.plusYears(1)
            }
            StatsPeriod.ALL_TIME -> {
                val start = Instant.EPOCH.atZone(zone)
                start to nowLocal.plusDays(1)
            }
        }
        return StatsRange(
            fromUtc = start.toUtcLocalDateTime(),
            toUtc = end.toUtcLocalDateTime(),
            fromLocal = start.toLocalDateTime(),
            toLocal = end.toLocalDateTime(),
            allTime = period == StatsPeriod.ALL_TIME,
        )
    }

    private fun ZonedDateTime.toUtcLocalDateTime(): LocalDateTime =
        withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()
}
