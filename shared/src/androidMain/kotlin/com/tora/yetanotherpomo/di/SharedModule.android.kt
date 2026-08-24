package com.tora.yetanotherpomo.di

import com.tora.yetanotherpomo.data.local.createFocusDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single { createFocusDataStore(androidContext()) }
}
