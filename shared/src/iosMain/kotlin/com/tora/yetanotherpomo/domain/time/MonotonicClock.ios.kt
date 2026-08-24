package com.tora.yetanotherpomo.domain.time

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.CLOCK_MONOTONIC
import platform.posix.clock_gettime_nsec_np

/**
 * Darwin's `CLOCK_MONOTONIC` is the counterpart to Android's `elapsedRealtime()`: per Apple's
 * `clock_gettime(3)`, it "will continue to increment while the system is asleep", and being
 * monotonic it ignores the user moving the system date.
 *
 * Do not pattern-match this from Linux experience - the names mean opposite things. On Linux,
 * `CLOCK_MONOTONIC` *stops* during suspend and `CLOCK_BOOTTIME` is the one that keeps counting.
 * On Darwin it is `CLOCK_UPTIME_RAW` that stops (Apple documents it as identical to
 * `mach_absolute_time()`). `NSProcessInfo.systemUptime` is built on that one, so it is wrong here
 * too, despite being the obvious-looking API.
 *
 * `clock_gettime_nsec_np` is Darwin-only ("np" = non-portable) and returns nanoseconds as a plain
 * `ULong`. That avoids both allocating a `timespec` and the `mach_timebase_info` tick conversion
 * that `mach_absolute_time`/`mach_continuous_time` would force on us - and, conveniently,
 * Kotlin/Native does not expose those two functions at all.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun systemMonotonicClock(): MonotonicClock = MonotonicClock {
    (clock_gettime_nsec_np(CLOCK_MONOTONIC.toUInt()) / 1_000_000uL).toLong()
}
