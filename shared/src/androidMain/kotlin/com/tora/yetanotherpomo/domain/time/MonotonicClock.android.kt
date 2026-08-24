package com.tora.yetanotherpomo.domain.time

import android.os.SystemClock

/**
 * [SystemClock.elapsedRealtime] is milliseconds since boot *including* deep sleep, and is not
 * affected by the user changing the system date. Already what FocusRepositoryImpl used.
 */
actual fun systemMonotonicClock(): MonotonicClock =
    MonotonicClock { SystemClock.elapsedRealtime() }
