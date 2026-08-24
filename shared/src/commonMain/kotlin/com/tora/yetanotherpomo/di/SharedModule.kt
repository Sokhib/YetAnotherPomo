package com.tora.yetanotherpomo.di

import com.tora.yetanotherpomo.data.repository.FocusRepositoryImpl
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.domain.time.systemMonotonicClock
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Everything the app needs that is the same on both platforms.
 *
 * `single` carries the guarantee the old AppContainer existed for: DataStore permits exactly one
 * live instance per file, and on Android the Activity and the AccessibilityService are separate
 * components that must share one - never open two against the same file.
 */
val sharedModule = module {
    single<FocusRepository> {
        FocusRepositoryImpl(dataStore = get(), clock = systemMonotonicClock())
    }
}

/**
 * The platform-shaped half of the graph. Android needs a Context to find `filesDir`; iOS asks
 * NSFileManager. Each target fills this in - see SharedModule.android.kt / SharedModule.ios.kt.
 */
expect fun platformModule(): Module

/**
 * Single entry point for both platforms. [appDeclaration] lets Android pass `androidContext(...)`
 * in without commonMain ever having to know what a Context is.
 */
fun initKoin(
    extraModules: List<Module>,
    appDeclaration: KoinApplication.() -> Unit,
): KoinApplication = startKoin {
    appDeclaration()
    modules(sharedModule, platformModule(), *extraModules.toTypedArray())
}

/**
 * No-argument overload, for Swift. Kotlin default arguments are not exported to Objective-C, so
 * without this iOS would have to spell out `doInitKoin(extraModules: [], appDeclaration: { _ in })`.
 */
fun initKoin(): KoinApplication = initKoin(emptyList()) {}
