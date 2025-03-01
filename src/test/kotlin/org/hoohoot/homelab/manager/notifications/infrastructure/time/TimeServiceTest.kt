package org.hoohoot.homelab.manager.notifications.infrastructure.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.*

class TimeServiceTest {

    @Test
    fun `getCurrentWeek should return correct Monday start and Sunday end of week for the current UTC date`() {
        // ARRANGE
        val timeService = TimeService()
        timeService.setFixedClock(Instant.parse("2023-10-18T12:34:56Z"), ZoneOffset.UTC)

        val expectedStart = ZonedDateTime.of(LocalDateTime.of(2023, 10, 16, 0, 0), ZoneOffset.UTC)
        val expectedEnd = ZonedDateTime.of(LocalDateTime.of(2023, 10, 22, 23, 59), ZoneOffset.UTC)

        // ACT
        val result = timeService.getCurrentWeek()

        // ASSERT
        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }

    @Test
    fun `getCurrentWeek should correctly handle week starting in one year and ending in the next`() {
        // ARRANGE
        val timeService = TimeService()
        timeService.setFixedClock(Instant.parse("2023-12-31T12:34:56Z"), ZoneOffset.UTC)

        val expectedStart = ZonedDateTime.of(LocalDateTime.of(2023, 12, 25, 0, 0), ZoneOffset.UTC)
        val expectedEnd = ZonedDateTime.of(LocalDateTime.of(2023, 12, 31, 23, 59), ZoneOffset.UTC)

        // ACT
        val result = timeService.getCurrentWeek()

        // ASSERT
        assertEquals(expectedStart, result.start)
        assertEquals(expectedEnd, result.end)
    }

}