package com.tora.yetanotherpomo.domain.repository

import com.tora.yetanotherpomo.domain.model.InstalledApp

interface InstalledAppsRepository {
    suspend fun getLaunchableApps(): List<InstalledApp>
}
