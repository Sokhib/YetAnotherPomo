package com.tora.yetanotherpomo

import android.app.Application
import com.tora.yetanotherpomo.di.AppContainer

class FocusLockApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
