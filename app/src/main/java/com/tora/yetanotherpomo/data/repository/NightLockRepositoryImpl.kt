package com.tora.yetanotherpomo.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.tora.yetanotherpomo.data.local.NightLockPreferencesKeys as K
import com.tora.yetanotherpomo.domain.model.ClockReading
import com.tora.yetanotherpomo.domain.model.Deadline
import com.tora.yetanotherpomo.domain.model.MINUTES_PER_DAY
import com.tora.yetanotherpomo.domain.model.NightLockConfig
import com.tora.yetanotherpomo.domain.model.NightLockState
import com.tora.yetanotherpomo.domain.model.NightSession
import com.tora.yetanotherpomo.domain.model.NightWindow
import com.tora.yetanotherpomo.domain.model.PendingNightChange
import com.tora.yetanotherpomo.domain.repository.NightClock
import com.tora.yetanotherpomo.domain.repository.NightLockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class NightLockRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val clock: NightClock,
) : NightLockRepository {

    override val stateFlow: Flow<NightLockState> = dataStore.data.map(::readState).distinctUntilChanged()

    override suspend fun refresh() = update { state, now -> state.advance(now) }

    override suspend fun reanchor() = update { state, now -> state.reanchor(now) }

    override suspend fun edit(transform: (NightLockConfig) -> NightLockConfig) = update { state, now ->
        val current = state.advance(now)
        current.edit(transform(current.desired), now)
    }

    override suspend fun discardPendingChange() = update { state, now -> state.discardPending(now) }

    /** Read, transform and write in one DataStore transaction; the clock is read inside it too. */
    private suspend fun update(transform: (NightLockState, ClockReading) -> NightLockState) {
        dataStore.edit { prefs ->
            val current = readState(prefs)
            val next = transform(current, clock.now())
            if (next != current) writeState(prefs, next)
        }
    }

    private fun readState(prefs: Preferences): NightLockState {
        val default = NightLockConfig.Default
        val config = NightLockConfig(
            enabled = prefs[K.ENABLED] ?: default.enabled,
            window = window(prefs[K.START_MINUTE], prefs[K.END_MINUTE], default.window),
            blockedPackages = prefs[K.BLOCKED] ?: default.blockedPackages,
        )
        val pending = prefs[K.PENDING_AT_WALL_MS]?.let { wallMs ->
            PendingNightChange(
                config = NightLockConfig(
                    enabled = prefs[K.PENDING_ENABLED] ?: config.enabled,
                    window = window(prefs[K.PENDING_START_MINUTE], prefs[K.PENDING_END_MINUTE], config.window),
                    blockedPackages = prefs[K.PENDING_BLOCKED] ?: config.blockedPackages,
                ),
                appliesAt = Deadline(
                    wallMs = wallMs,
                    elapsedMs = prefs[K.PENDING_AT_ELAPSED_MS] ?: 0L,
                    bootCount = prefs[K.PENDING_AT_BOOT] ?: ClockReading.UNKNOWN_BOOT,
                ),
            )
        }
        val session = prefs[K.SESSION_UNTIL_WALL_MS]?.let { wallMs ->
            NightSession(
                blockedPackages = prefs[K.SESSION_BLOCKED] ?: emptySet(),
                until = Deadline(
                    wallMs = wallMs,
                    elapsedMs = prefs[K.SESSION_UNTIL_ELAPSED_MS] ?: 0L,
                    bootCount = prefs[K.SESSION_UNTIL_BOOT] ?: ClockReading.UNKNOWN_BOOT,
                ),
            )
        }
        return NightLockState(config, pending, session)
    }

    private fun writeState(prefs: MutablePreferences, state: NightLockState) {
        prefs[K.ENABLED] = state.config.enabled
        prefs[K.START_MINUTE] = state.config.window.startMinute
        prefs[K.END_MINUTE] = state.config.window.endMinute
        prefs[K.BLOCKED] = state.config.blockedPackages

        val pending = state.pending
        if (pending == null) {
            prefs.remove(K.PENDING_ENABLED)
            prefs.remove(K.PENDING_START_MINUTE)
            prefs.remove(K.PENDING_END_MINUTE)
            prefs.remove(K.PENDING_BLOCKED)
            prefs.remove(K.PENDING_AT_WALL_MS)
            prefs.remove(K.PENDING_AT_ELAPSED_MS)
            prefs.remove(K.PENDING_AT_BOOT)
        } else {
            prefs[K.PENDING_ENABLED] = pending.config.enabled
            prefs[K.PENDING_START_MINUTE] = pending.config.window.startMinute
            prefs[K.PENDING_END_MINUTE] = pending.config.window.endMinute
            prefs[K.PENDING_BLOCKED] = pending.config.blockedPackages
            prefs[K.PENDING_AT_WALL_MS] = pending.appliesAt.wallMs
            prefs[K.PENDING_AT_ELAPSED_MS] = pending.appliesAt.elapsedMs
            prefs[K.PENDING_AT_BOOT] = pending.appliesAt.bootCount
        }

        val session = state.session
        if (session == null) {
            prefs.remove(K.SESSION_BLOCKED)
            prefs.remove(K.SESSION_UNTIL_WALL_MS)
            prefs.remove(K.SESSION_UNTIL_ELAPSED_MS)
            prefs.remove(K.SESSION_UNTIL_BOOT)
        } else {
            prefs[K.SESSION_BLOCKED] = session.blockedPackages
            prefs[K.SESSION_UNTIL_WALL_MS] = session.until.wallMs
            prefs[K.SESSION_UNTIL_ELAPSED_MS] = session.until.elapsedMs
            prefs[K.SESSION_UNTIL_BOOT] = session.until.bootCount
        }
    }

    private fun window(start: Int?, end: Int?, fallback: NightWindow): NightWindow = NightWindow(
        startMinute = (start ?: fallback.startMinute).coerceIn(0, MINUTES_PER_DAY - 1),
        endMinute = (end ?: fallback.endMinute).coerceIn(0, MINUTES_PER_DAY - 1),
    )
}
