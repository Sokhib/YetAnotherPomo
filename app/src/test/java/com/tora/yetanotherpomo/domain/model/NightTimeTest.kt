package com.tora.yetanotherpomo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

class NightTimeTest {

    private val night = NightWindow(startMinute = 22 * 60, endMinute = 7 * 60)
    private val lunch = NightWindow(startMinute = 13 * 60, endMinute = 14 * 60)

    @Test
    fun `window crossing midnight contains its start and excludes its end`() {
        assertTrue(night.contains(22 * 60))
        assertTrue(night.contains(0))
        assertTrue(night.contains(7 * 60 - 1))
        assertFalse(night.contains(7 * 60))
        assertFalse(night.contains(22 * 60 - 1))
        assertFalse(night.contains(12 * 60))
    }

    @Test
    fun `same-day window contains only its own minutes`() {
        assertTrue(lunch.contains(13 * 60))
        assertTrue(lunch.contains(14 * 60 - 1))
        assertFalse(lunch.contains(14 * 60))
        assertFalse(lunch.contains(12 * 60 + 59))
    }

    @Test
    fun `empty window never contains anything`() {
        val empty = NightWindow(600, 600)
        assertTrue(empty.isEmpty)
        assertFalse((0 until MINUTES_PER_DAY).any(empty::contains))
        assertNull(empty.endOfCurrentOrNextOccurrence(at(12)))
        assertNull(empty.nextStart(at(12)))
    }

    @Test
    fun `covers compares every minute`() {
        assertTrue(NightWindow(21 * 60, 8 * 60).covers(night))
        assertTrue(night.covers(night))
        assertFalse(night.covers(NightWindow(21 * 60, 7 * 60)))
        assertFalse(night.covers(NightWindow(22 * 60, 8 * 60)))
        assertFalse(night.covers(lunch))
        assertTrue(night.covers(NightWindow(300, 300)))
        assertFalse(NightWindow(300, 300).covers(night))
    }

    @Test
    fun `occurrence end is tomorrow before midnight and today after it`() {
        assertEquals(wall(7, day = 1), night.endOfOccurrenceContaining(at(23)))
        assertEquals(wall(7, day = 1), night.endOfOccurrenceContaining(at(22)))
        assertEquals(wall(7, day = 1), night.endOfOccurrenceContaining(at(3, day = 1)))
    }

    @Test
    fun `current or next occurrence end looks ahead when outside the window`() {
        assertEquals(wall(7, day = 1), night.endOfCurrentOrNextOccurrence(at(15)))
        assertEquals(wall(7, day = 1), night.endOfCurrentOrNextOccurrence(at(8)))
        assertEquals(wall(14), lunch.endOfCurrentOrNextOccurrence(at(12)))
        assertEquals(wall(14), lunch.endOfCurrentOrNextOccurrence(at(13, 30)))
        assertEquals(wall(14, day = 1), lunch.endOfCurrentOrNextOccurrence(at(15)))
    }

    @Test
    fun `next start is today until it passes`() {
        assertEquals(wall(22), night.nextStart(at(15)))
        assertEquals(wall(22, day = 1), night.nextStart(at(23)))
    }

    @Test
    fun `wall time follows local clocks across a spring-forward night`() {
        // US clocks go from 02:00 to 03:00 on Sunday 8 March 2026: that night is an hour shorter.
        val newYork = TimeZone.getTimeZone("America/New_York")
        val saturday23 = GregorianCalendar(newYork).apply {
            clear()
            set(2026, Calendar.MARCH, 7, 23, 0)
        }.timeInMillis
        val reading = ClockReading(wallMs = saturday23, elapsedMs = 0, bootCount = BOOT, timeZone = newYork)

        val end = night.endOfOccurrenceContaining(reading)

        assertEquals(7 * HOUR_MS, end - saturday23)
        val local = GregorianCalendar(newYork).apply { timeInMillis = end }
        assertEquals(7, local.get(Calendar.HOUR_OF_DAY))
        assertEquals(8, local.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `minute of day is local`() {
        val tashkent = TimeZone.getTimeZone("Asia/Tashkent") // UTC+5, no DST
        val reading = ClockReading(wallMs = wall(18), elapsedMs = 0, bootCount = BOOT, timeZone = tashkent)
        assertEquals(23 * 60, reading.minuteOfDay)
    }

    @Test
    fun `deadline needs both clocks within one boot`() {
        val deadline = Deadline.at(wall(7, day = 1), at(23))

        assertFalse(deadline.hasPassed(at(6, 59, day = 1)))
        assertTrue(deadline.hasPassed(at(7, day = 1)))
        // Wall clock wound eight hours ahead at 23:30: the monotonic clock still says 23:30.
        assertFalse(deadline.hasPassed(at(23, 30, wallSkewMs = 8 * HOUR_MS)))
        // Wall clock wound back: later on the wall, so later in practice too.
        assertFalse(deadline.hasPassed(at(7, 30, day = 1, wallSkewMs = -2 * HOUR_MS)))
    }

    @Test
    fun `after a reboot only the wall clock counts`() {
        val deadline = Deadline.at(wall(7, day = 1), at(23))

        assertFalse(deadline.hasPassed(afterReboot(6, day = 1)))
        assertTrue(deadline.hasPassed(afterReboot(7, day = 1)))
    }

    @Test
    fun `unknown boot count falls back to the wall clock`() {
        val unknown = at(23).copy(bootCount = ClockReading.UNKNOWN_BOOT)
        val deadline = Deadline.at(wall(7, day = 1), unknown)

        assertTrue(deadline.hasPassed(at(23, 30, wallSkewMs = 8 * HOUR_MS).copy(bootCount = ClockReading.UNKNOWN_BOOT)))
    }

    @Test
    fun `later keeps the first deadline on a tie`() {
        val a = Deadline(wallMs = 10, elapsedMs = 1, bootCount = BOOT)
        val b = Deadline(wallMs = 10, elapsedMs = 2, bootCount = BOOT)
        val c = Deadline(wallMs = 11, elapsedMs = 0, bootCount = BOOT)
        assertEquals(a, later(a, b))
        assertEquals(c, later(a, c))
        assertEquals(c, later(c, a))
    }
}
