package com.tora.yetanotherpomo.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tora.yetanotherpomo.FocusLockApplication
import com.tora.yetanotherpomo.data.time.SystemTimeSettings
import kotlinx.coroutines.launch

/**
 * Re-checks Night lock after a boot, an app update, and every clock or time-zone change, even
 * when the blocker isn't running: arms a lock that is due and applies a change that is due.
 *
 * The blocker re-checks the same events itself while it runs; both writes are idempotent.
 */
class NightLockSystemReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val trustedClockChange = when (intent.action) {
            Intent.ACTION_TIME_CHANGED -> SystemTimeSettings.isNetworkTimeOn(context)
            Intent.ACTION_TIMEZONE_CHANGED -> SystemTimeSettings.isNetworkTimeZoneOn(context)
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> false
            else -> return
        }
        val container = (context.applicationContext as FocusLockApplication).container
        val pendingResult = goAsync()
        container.applicationScope.launch {
            try {
                // A change made by hand never re-derives the lock: the running lock's end was
                // pinned to the monotonic clock exactly so that it can't be moved this way.
                if (trustedClockChange) {
                    container.nightLockRepository.reanchor()
                } else {
                    container.nightLockRepository.refresh()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
