package com.tora.yetanotherpomo.domain.model

import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

internal const val MINUTE_MS = 60_000L
internal const val HOUR_MS = 60 * MINUTE_MS
internal const val DAY_MS = 24 * HOUR_MS

internal val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/** Monday 16 Nov 2026, 00:00 UTC: day 0 of every scenario. */
internal val DAY0_MIDNIGHT: Long = GregorianCalendar(UTC).apply {
    clear()
    set(2026, Calendar.NOVEMBER, 16)
}.timeInMillis

internal const val BOOT = 7
private const val ELAPSED_AT_DAY0 = 50 * DAY_MS

/** Real time at [hour]:[minute] on [day], as an offset on the wall clock. */
internal fun wall(hour: Int, minute: Int = 0, day: Int = 0): Long =
    DAY0_MIDNIGHT + day * DAY_MS + hour * HOUR_MS + minute * MINUTE_MS

/**
 * A reading at real time [hour]:[minute] on [day]. [wallSkewMs] winds the wall clock away from
 * real time, the way changing it in Date & time would; the monotonic clock never moves with it.
 */
internal fun at(
    hour: Int,
    minute: Int = 0,
    day: Int = 0,
    wallSkewMs: Long = 0,
    boot: Int = BOOT,
): ClockReading {
    val real = wall(hour, minute, day)
    return ClockReading(
        wallMs = real + wallSkewMs,
        elapsedMs = ELAPSED_AT_DAY0 + (real - DAY0_MIDNIGHT),
        bootCount = boot,
        timeZone = UTC,
    )
}

/** A reading right after a reboot: new boot count, monotonic clock restarted near zero. */
internal fun afterReboot(hour: Int, minute: Int = 0, day: Int = 0, wallSkewMs: Long = 0): ClockReading =
    ClockReading(
        wallMs = wall(hour, minute, day) + wallSkewMs,
        elapsedMs = 2 * MINUTE_MS,
        bootCount = BOOT + 1,
        timeZone = UTC,
    )
