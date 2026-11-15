package com.tora.yetanotherpomo.domain.repository

/** The device settings that make Night lock hard to switch off. Read fresh on every call. */
interface NightProtectionChecker {
    /** Focus Lock is an active device admin, so it can't be uninstalled until that is switched off. */
    fun isUninstallProtectionOn(): Boolean

    /** Exempt from battery optimisation, so the phone maker's task killer leaves the blocker running. */
    fun isBatteryUnrestricted(): Boolean
}
