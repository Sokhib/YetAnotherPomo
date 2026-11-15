package com.tora.yetanotherpomo.data.time

import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import com.tora.yetanotherpomo.domain.model.ClockReading
import com.tora.yetanotherpomo.domain.repository.NightClock
import java.util.TimeZone

class SystemNightClock(context: Context) : NightClock {
    private val appContext = context.applicationContext

    // Read once: the boot count can't change while this process is alive, because a reboot ends it.
    private val bootCount: Int by lazy {
        Settings.Global.getInt(appContext.contentResolver, Settings.Global.BOOT_COUNT, ClockReading.UNKNOWN_BOOT)
    }

    override fun now(): ClockReading = ClockReading(
        wallMs = System.currentTimeMillis(),
        elapsedMs = SystemClock.elapsedRealtime(),
        bootCount = bootCount,
        // Android resets the process default on ACTION_TIMEZONE_CHANGED, so this follows travel.
        timeZone = TimeZone.getDefault(),
    )
}

/**
 * Whether a clock change came from the network. While "Set time automatically" is on, the time
 * can't be changed by hand, so a change is a correction to trust, not a way around the lock.
 */
object SystemTimeSettings {
    fun isNetworkTimeOn(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME, 0) == 1

    fun isNetworkTimeZoneOn(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.AUTO_TIME_ZONE, 0) == 1
}
