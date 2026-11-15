package com.tora.yetanotherpomo.data.repository

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.PowerManager
import com.tora.yetanotherpomo.domain.repository.NightProtectionChecker
import com.tora.yetanotherpomo.receiver.NightLockAdminReceiver

class NightProtectionCheckerImpl(
    private val context: Context,
) : NightProtectionChecker {

    private val adminComponent = ComponentName(context, NightLockAdminReceiver::class.java)

    override fun isUninstallProtectionOn(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isAdminActive(adminComponent)
    }

    override fun isBatteryUnrestricted(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }
}
