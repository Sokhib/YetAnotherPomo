package com.tora.yetanotherpomo.domain.model

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

const val MINUTES_PER_DAY = 24 * 60

/**
 * One reading of every clock Night lock cares about, taken together.
 *
 * The wall clock ([wallMs]) decides *when* the lock starts, because "22:00" is a wall-clock idea.
 * The monotonic clock ([elapsedMs], `SystemClock.elapsedRealtime`) decides when a running lock
 * may end, because it can't be wound forward from Settings. [bootCount] says whether an
 * [elapsedMs] taken earlier is still comparable: the monotonic clock restarts at every boot.
 *
 * Pure JVM types only, so the schedule logic is unit-testable without Android.
 */
data class ClockReading(
    val wallMs: Long,
    val elapsedMs: Long,
    val bootCount: Int,
    val timeZone: TimeZone,
) {
    /** Minutes after local midnight, 0..1439. */
    val minuteOfDay: Int = calendar().let { it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE) }

    /**
     * The wall-clock instant of [minuteOfDay] on the local day [dayOffset] days from this
     * reading's day. A local time that doesn't exist (the hour skipped when clocks go forward)
     * moves forward, which is what a person means by "07:00" on that morning.
     */
    fun wallAt(dayOffset: Int, minuteOfDay: Int): Long = calendar().run {
        add(Calendar.DATE, dayOffset)
        set(Calendar.HOUR_OF_DAY, minuteOfDay / 60)
        set(Calendar.MINUTE, minuteOfDay % 60)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    // Gregorian and Locale.ROOT explicitly: Calendar.getInstance() follows the default locale
    // and can hand back a Buddhist or Japanese-imperial calendar.
    private fun calendar(): Calendar = GregorianCalendar(timeZone, Locale.ROOT).apply { timeInMillis = wallMs }

    companion object {
        /** The platform couldn't report a boot count; only the wall clock can be trusted. */
        const val UNKNOWN_BOOT = -1
    }
}

/**
 * A moment that has passed only once *both* clocks agree it has.
 *
 * Winding the wall clock forward satisfies [wallMs] but not [elapsedMs], so a running lock can't
 * be ended early from Date & time. Winding it back delays [wallMs], which only makes the lock
 * longer. After a reboot [elapsedMs] means nothing, and the wall clock alone decides.
 */
data class Deadline(
    val wallMs: Long,
    val elapsedMs: Long,
    val bootCount: Int,
) {
    fun hasPassed(now: ClockReading): Boolean {
        if (now.wallMs < wallMs) return false
        val sameBoot = bootCount != ClockReading.UNKNOWN_BOOT && bootCount == now.bootCount
        return !sameBoot || now.elapsedMs >= elapsedMs
    }

    companion object {
        /** The deadline at wall time [wallMs], pinned to the monotonic clock as read at [now]. */
        fun at(wallMs: Long, now: ClockReading): Deadline =
            Deadline(wallMs = wallMs, elapsedMs = now.elapsedMs + (wallMs - now.wallMs), bootCount = now.bootCount)
    }
}

/** The later of two deadlines by wall time; [a] on a tie, so re-deriving the same end is a no-op. */
fun later(a: Deadline, b: Deadline): Deadline = if (b.wallMs > a.wallMs) b else a

/**
 * A daily window on the local wall clock: `[startMinute, endMinute)` in minutes after midnight.
 * An [endMinute] before [startMinute] crosses midnight (22:00–07:00). Equal values are an empty
 * window that never starts; the editor doesn't allow it, and the model refuses to guess.
 */
data class NightWindow(
    val startMinute: Int,
    val endMinute: Int,
) {
    init {
        require(startMinute in 0 until MINUTES_PER_DAY) { "startMinute out of range: $startMinute" }
        require(endMinute in 0 until MINUTES_PER_DAY) { "endMinute out of range: $endMinute" }
    }

    val isEmpty: Boolean get() = startMinute == endMinute

    private val crossesMidnight: Boolean get() = endMinute < startMinute

    fun contains(minuteOfDay: Int): Boolean = when {
        isEmpty -> false
        crossesMidnight -> minuteOfDay >= startMinute || minuteOfDay < endMinute
        else -> minuteOfDay in startMinute until endMinute
    }

    /** True when every minute of [other] is inside this window too. */
    fun covers(other: NightWindow): Boolean = other.minutes().all(::contains)

    /** Wall-clock end of the occurrence that contains [now]. Only meaningful when [contains] it. */
    fun endOfOccurrenceContaining(now: ClockReading): Long {
        val endsTomorrow = crossesMidnight && now.minuteOfDay >= startMinute
        return now.wallAt(if (endsTomorrow) 1 else 0, endMinute)
    }

    /** Wall-clock end of the occurrence running at [now], or else of the next one. Null if empty. */
    fun endOfCurrentOrNextOccurrence(now: ClockReading): Long? {
        if (isEmpty) return null
        val minute = now.minuteOfDay
        if (contains(minute)) return endOfOccurrenceContaining(now)
        // Not inside: the next occurrence starts today if its start is still ahead and the window
        // ends the same day; otherwise it ends tomorrow.
        val endsToday = !crossesMidnight && minute < startMinute
        return now.wallAt(if (endsToday) 0 else 1, endMinute)
    }

    /** Wall-clock start of the next occurrence that hasn't started yet. Null if empty. */
    fun nextStart(now: ClockReading): Long? {
        if (isEmpty) return null
        return now.wallAt(if (now.minuteOfDay < startMinute) 0 else 1, startMinute)
    }

    private fun minutes(): Sequence<Int> {
        val length = (endMinute - startMinute + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return (0 until length).asSequence().map { (startMinute + it) % MINUTES_PER_DAY }
    }
}
