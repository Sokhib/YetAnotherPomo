package com.tora.yetanotherpomo.di

import android.content.Context
import com.tora.yetanotherpomo.data.apps.InstalledAppsRepositoryImpl
import com.tora.yetanotherpomo.data.local.buildFocusDataStore
import com.tora.yetanotherpomo.data.repository.AccessibilityStatusCheckerImpl
import com.tora.yetanotherpomo.data.repository.FocusRepositoryImpl
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository

/**
 * Manual dependency container (no Hilt/KSP, to keep build risk low). Built once in
 * [com.tora.yetanotherpomo.FocusLockApplication.onCreate], which the platform guarantees runs
 * before any Service's onCreate/onServiceConnected - so both the Activity/ViewModel side and
 * FocusAccessibilityService always reach the same singleton [focusRepository] instance, never
 * opening a second DataStore against the same file.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val focusRepository: FocusRepository by lazy {
        FocusRepositoryImpl(dataStore = buildFocusDataStore(appContext))
    }

    val installedAppsRepository: InstalledAppsRepository by lazy {
        InstalledAppsRepositoryImpl(appContext)
    }

    val accessibilityStatusChecker: AccessibilityStatusChecker by lazy {
        AccessibilityStatusCheckerImpl(appContext)
    }
}
