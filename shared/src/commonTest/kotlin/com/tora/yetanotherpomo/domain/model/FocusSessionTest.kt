package com.tora.yetanotherpomo.domain.model

import com.tora.yetanotherpomo.domain.time.MonotonicClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Lives in commonTest, so it runs against every target: the JVM on Android and the real
 * Kotlin/Native runtime on iOS. Behaviour is asserted once and verified everywhere.
 */
class FocusSessionTest {

    private fun session(minutes: Int = 25, startedAt: Long = 1_000L) = FocusSession(
        minutes = minutes,
        endElapsedRealtimeMs = startedAt + minutes * 60_000L,
        totalSeconds = minutes * 60,
    )

    @Test
    fun countsDownAsTheClockAdvances() {
        val s = session(minutes = 25, startedAt = 1_000L)
        assertEquals(25 * 60, s.remainingSeconds(nowElapsedRealtimeMs = 1_000L))
        assertEquals(24 * 60, s.remainingSeconds(nowElapsedRealtimeMs = 1_000L + 60_000L))
        assertTrue(s.isRunning(nowElapsedRealtimeMs = 1_000L))
    }

    @Test
    fun isNotRunningOnceTheEndPasses() {
        val s = session(minutes = 5, startedAt = 0L)
        assertFalse(s.isRunning(nowElapsedRealtimeMs = 5 * 60_000L))
        assertEquals(0, s.remainingSeconds(nowElapsedRealtimeMs = 10 * 60_000L))
    }

    /**
     * Both platforms measure from the last boot, so after a reboot `now` restarts near zero while
     * the stored end time is still large - which would otherwise read as a session with more time
     * left than it ever had. Clamping to totalSeconds is what turns that into an expired session.
     */
    @Test
    fun treatsARebootAsAnExpiredSession() {
        val beforeReboot = session(minutes = 25, startedAt = 9_000_000L)
        val justAfterBoot = 500L
        assertEquals(25 * 60, beforeReboot.remainingSeconds(justAfterBoot))
    }

    /**
     * The point of MonotonicClock being an interface rather than a bare `expect fun`: production
     * passes systemMonotonicClock(), a test passes whatever it likes and steps time by hand.
     */
    @Test
    fun fakeClockDrivesTheSessionWithoutAnyRealTimePassing() {
        var now = 0L
        val clock = MonotonicClock { now }
        val s = session(minutes = 1, startedAt = clock.elapsedRealtimeMs())

        assertTrue(s.isRunning(clock.elapsedRealtimeMs()))
        now += 59_000L
        assertEquals(1, s.remainingSeconds(clock.elapsedRealtimeMs()))
        now += 1_000L
        assertFalse(s.isRunning(clock.elapsedRealtimeMs()))
    }
}
