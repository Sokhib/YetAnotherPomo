package com.tora.yetanotherpomo.domain.repository

import com.tora.yetanotherpomo.domain.model.ClockReading
import com.tora.yetanotherpomo.domain.model.NightLockConfig
import com.tora.yetanotherpomo.domain.model.NightLockState
import kotlinx.coroutines.flow.Flow

/** Reads every clock at once. The Android implementation is the only one that touches the platform. */
fun interface NightClock {
    fun now(): ClockReading
}

/**
 * Night lock's persisted [NightLockState]. Every write reads the clock and applies the
 * [NightLockState] rules inside one atomic store transaction, so the UI, the blocker and the
 * system receivers can all write without racing each other.
 */
interface NightLockRepository {
    val stateFlow: Flow<NightLockState>

    /** Persists [NightLockState.advance]: arms a lock that is due, applies a change that is due. */
    suspend fun refresh()

    /**
     * The clock was corrected by the network, so it can be trusted again: re-derive the running
     * lock from scratch instead of keeping an end that was computed from the wrong time.
     */
    suspend fun reanchor()

    /** Edits what the person asked for; see [NightLockState.edit] for when it takes effect. */
    suspend fun edit(transform: (NightLockConfig) -> NightLockConfig)

    suspend fun discardPendingChange()
}
