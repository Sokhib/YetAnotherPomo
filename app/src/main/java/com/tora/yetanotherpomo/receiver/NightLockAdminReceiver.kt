package com.tora.yetanotherpomo.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.tora.yetanotherpomo.R

/**
 * Makes Focus Lock a plain device admin, which uses no policies at all. Being an active admin is
 * the whole point: Android refuses to uninstall it until this is switched off, and Night lock
 * keeps the page that switches it off closed during the night.
 */
class NightLockAdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence =
        context.getString(R.string.night_lock_admin_disable_warning)
}
