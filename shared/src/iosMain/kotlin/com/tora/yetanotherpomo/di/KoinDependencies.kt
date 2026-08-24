package com.tora.yetanotherpomo.di

import com.tora.yetanotherpomo.domain.repository.FocusRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Swift cannot use Koin's `by inject()` or reified `get<T>()` - both rely on Kotlin generics that
 * do not survive the Objective-C bridge. The standard workaround is this: one Kotlin class that
 * resolves everything on the Kotlin side and exposes it as plain properties, which the framework
 * exports as ordinary Swift getters.
 *
 *     let deps = KoinDependencies()
 *     let repo = deps.focusRepository
 */
class KoinDependencies : KoinComponent {
    val focusRepository: FocusRepository by inject()
}
