package com.tora.yetanotherpomo.domain.repository

import com.tora.yetanotherpomo.domain.model.FocusSession
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import kotlinx.coroutines.flow.Flow

/**
 * The single source of truth for session/allowlist/switch state, shared by [MainActivity]-side
 * ViewModels and the background AccessibilityService. Exposes three independent flows (rather
 * than one combined state) so a screen that only cares about, say, the allowlist doesn't
 * recompose on every 1-second timer tick.
 */
interface FocusRepository {
    val sessionFlow: Flow<FocusSession>
    val allowedFlow: Flow<Set<String>>
    val switchesFlow: Flow<FocusSwitches>

    val holdToEndMs: Int
    val dialMaxMinutes: Int

    suspend fun setMinutes(minutes: Int)
    suspend fun startSession(minutes: Int)
    suspend fun endSession()
    suspend fun setAppAllowed(packageName: String, allowed: Boolean)
    suspend fun setSwitch(update: (FocusSwitches) -> FocusSwitches)
}
