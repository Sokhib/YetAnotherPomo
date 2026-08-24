package com.tora.yetanotherpomo.di

import com.tora.yetanotherpomo.data.local.createFocusDataStore
import com.tora.yetanotherpomo.domain.model.InstalledApp
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS cannot enumerate installed apps and has no accessibility service to enable, so both
 * contracts resolve to honest do-nothing implementations. That keeps FocusViewModel shareable
 * as-is; SwiftUI simply never renders the allowlist or the permission banner.
 */
private object NoBlockableApps : InstalledAppsRepository {
    override suspend fun getLaunchableApps(): List<InstalledApp> = emptyList()
}

private object BlockingUnavailable : AccessibilityStatusChecker {
    override fun isServiceEnabled(): Boolean = false
}

actual fun platformModule(): Module = module {
    single { createFocusDataStore() }
    single<InstalledAppsRepository> { NoBlockableApps }
    single<AccessibilityStatusChecker> { BlockingUnavailable }
}
