package com.tora.yetanotherpomo.di

import android.content.Context
import com.tora.yetanotherpomo.data.apps.InstalledAppsRepositoryImpl
import com.tora.yetanotherpomo.data.local.buildFocusDataStore
import com.tora.yetanotherpomo.data.repository.AccessibilityStatusCheckerImpl
import com.tora.yetanotherpomo.data.repository.FocusRepositoryImpl
import com.tora.yetanotherpomo.data.repository.NightLockRepositoryImpl
import com.tora.yetanotherpomo.data.repository.NightProtectionCheckerImpl
import com.tora.yetanotherpomo.data.time.SystemNightClock
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import com.tora.yetanotherpomo.domain.repository.NightClock
import com.tora.yetanotherpomo.domain.repository.NightLockRepository
import com.tora.yetanotherpomo.domain.repository.NightProtectionChecker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency container (no Hilt/KSP, to keep build risk low). Built once in
 * [com.tora.yetanotherpomo.FocusLockApplication.onCreate], which the platform guarantees runs
 * before any Service's onCreate/onServiceConnected or any receiver's onReceive - so the
 * Activity/ViewModel side, FocusAccessibilityService and the receivers always reach the same
 * singletons, and the one [dataStore] is never opened twice against the same file.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Shared by both repositories: DataStore allows a single active instance per file. */
    private val dataStore by lazy { buildFocusDataStore(appContext) }

    /** For work that must outlive a screen, such as a receiver's goAsync() write. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val focusRepository: FocusRepository by lazy {
        FocusRepositoryImpl(dataStore = dataStore)
    }

    val nightClock: NightClock by lazy { SystemNightClock(appContext) }

    val nightLockRepository: NightLockRepository by lazy {
        NightLockRepositoryImpl(dataStore = dataStore, clock = nightClock)
    }

    val nightProtectionChecker: NightProtectionChecker by lazy {
        NightProtectionCheckerImpl(appContext)
    }

    val installedAppsRepository: InstalledAppsRepository by lazy {
        InstalledAppsRepositoryImpl(appContext)
    }

    val accessibilityStatusChecker: AccessibilityStatusChecker by lazy {
        AccessibilityStatusCheckerImpl(appContext)
    }
}
