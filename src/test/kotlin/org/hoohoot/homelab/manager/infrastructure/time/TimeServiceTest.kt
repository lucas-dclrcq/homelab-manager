package org.hoohoot.homelab.manager.infrastructure.time

import kotlinx.datetime.*
import org.hoohoot.homelab.manager.application.ports.Week
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.time.Duration

class TimeServiceTest {

    @Test
    fun `getCurrentWeek returns correct week for a fixed clock representing Monday`() {
        val fixedInstant = Instant.parse("2023-10-09T00:00:00Z") // Monday
        val timeService = TimeService()
        timeService.setFixedClock(fixedInstant)

        val expectedStart = Instant.parse("2023-10-09T00:00:00Z")
        val expectedEnd = Instant.parse("2023-10-15T23:59:00Z")

        val result: Week = timeService.getCurrentWeek()

        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }

    @Test
    fun `getCurrentWeek returns correct week for a fixed clock representing Wednesday`() {
        val fixedInstant = Instant.parse("2023-10-11T12:00:00Z") // Wednesday
        val timeService = TimeService()
        timeService.setFixedClock(fixedInstant)

        val expectedStart = Instant.parse("2023-10-09T00:00:00Z")
        val expectedEnd = Instant.parse("2023-10-15T23:59:00Z")

        val result: Week = timeService.getCurrentWeek()

        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }

    @Test
    fun `getCurrentWeek returns correct week for a fixed clock representing Sunday`() {
        val fixedInstant = Instant.parse("2023-10-15T18:00:00Z") // Sunday
        val timeService = TimeService()
        timeService.setFixedClock(fixedInstant)

        val expectedStart = Instant.parse("2023-10-09T00:00:00Z")
        val expectedEnd = Instant.parse("2023-10-15T23:59:00Z")

        val result: Week = timeService.getCurrentWeek()

        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }

    @Test
    fun `getCurrentWeek returns correct week for a fixed clock spanning two different UTC dates`() {
        val fixedInstant = Instant.parse("2023-10-09T23:59:59Z") // Just before Tuesday
        val timeService = TimeService()
        timeService.setFixedClock(fixedInstant)

        val expectedStart = Instant.parse("2023-10-09T00:00:00Z")
        val expectedEnd = Instant.parse("2023-10-15T23:59:00Z")

        val result: Week = timeService.getCurrentWeek()

        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }
}