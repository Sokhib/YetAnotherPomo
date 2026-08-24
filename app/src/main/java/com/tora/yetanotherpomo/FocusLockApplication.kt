package com.tora.yetanotherpomo

import android.app.Application
import com.tora.yetanotherpomo.di.androidModule
import com.tora.yetanotherpomo.di.initKoin
import org.koin.android.ext.koin.androidContext

/**
 * Application.onCreate is guaranteed to run before any Activity or Service, so starting Koin here
 * means both MainActivity and FocusAccessibilityService resolve the same singletons - and in
 * particular the same DataStore.
 */
class FocusLockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(extraModules = listOf(androidModule)) {
            androidContext(this@FocusLockApplication)
        }
    }
}
