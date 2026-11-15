package com.tora.yetanotherpomo.domain.repository

import com.tora.yetanotherpomo.domain.model.InstalledApp

interface InstalledAppsRepository {
    suspend fun getLaunchableApps(): List<InstalledApp>

    /** Home, Settings, keyboards and this app: never blocked, so not worth offering in a picker. */
    suspend fun getAlwaysReachablePackages(): Set<String>
}
