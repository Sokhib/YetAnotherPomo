package com.tora.yetanotherpomo.di

import com.tora.yetanotherpomo.data.apps.InstalledAppsRepositoryImpl
import com.tora.yetanotherpomo.data.repository.AccessibilityStatusCheckerImpl
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import com.tora.yetanotherpomo.ui.FocusViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * The half of the graph that only exists on Android: reading the installed-app list, and checking
 * whether the user has switched the accessibility service on. iOS binds the same two contracts to
 * no-ops in :shared.
 */
val androidModule = module {
    single<InstalledAppsRepository> { InstalledAppsRepositoryImpl(androidContext()) }
    single<AccessibilityStatusChecker> { AccessibilityStatusCheckerImpl(androidContext()) }

    // Koin builds the ViewModel and hands it to androidx's ViewModelStore, replacing the
    // hand-written ViewModelProvider.Factory. Moves to :shared in step 6.
    viewModel { FocusViewModel(get(), get(), get()) }
}
