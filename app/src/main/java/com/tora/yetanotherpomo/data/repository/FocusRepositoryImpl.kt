package com.tora.yetanotherpomo.data.repository

import android.os.SystemClock
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.tora.yetanotherpomo.data.local.FocusPreferencesKeys
import com.tora.yetanotherpomo.domain.model.FocusSession
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FocusRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    override val holdToEndMs: Int = FocusPreferencesKeys.DEFAULT_HOLD_TO_END_MS,
    override val dialMaxMinutes: Int = FocusPreferencesKeys.DEFAULT_DIAL_MAX_MINUTES,
) : FocusRepository {

    override val sessionFlow: Flow<FocusSession> = dataStore.data.map { prefs ->
        val minutes = prefs[FocusPreferencesKeys.SESSION_MINUTES] ?: FocusPreferencesKeys.DEFAULT_MINUTES
        val endMs = prefs[FocusPreferencesKeys.SESSION_END_ELAPSED_REALTIME_MS]
        val totalSeconds = prefs[FocusPreferencesKeys.SESSION_TOTAL_SECONDS] ?: (minutes * 60)
        FocusSession(
            minutes = minutes,
            endElapsedRealtimeMs = endMs?.takeIf { it != FocusPreferencesKeys.NO_SESSION },
            totalSeconds = totalSeconds,
        )
    }.distinctUntilChanged()

    override val allowedFlow: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[FocusPreferencesKeys.ALLOWED_PACKAGES] ?: emptySet()
    }.distinctUntilChanged()

    override val switchesFlow: Flow<FocusSwitches> = dataStore.data.map { prefs ->
        FocusSwitches(
            strict = prefs[FocusPreferencesKeys.SWITCH_STRICT] ?: true,
            holdToBreakOut = prefs[FocusPreferencesKeys.SWITCH_HOLD] ?: true,
            chime = prefs[FocusPreferencesKeys.SWITCH_CHIME] ?: false,
            keepClockVisible = prefs[FocusPreferencesKeys.SWITCH_CLOCK] ?: true,
        )
    }.distinctUntilChanged()

    override suspend fun setMinutes(minutes: Int) {
        dataStore.edit { it[FocusPreferencesKeys.SESSION_MINUTES] = minutes }
    }

    override suspend fun startSession(minutes: Int) {
        val end = SystemClock.elapsedRealtime() + minutes * 60_000L
        dataStore.edit { prefs ->
            prefs[FocusPreferencesKeys.SESSION_MINUTES] = minutes
            prefs[FocusPreferencesKeys.SESSION_END_ELAPSED_REALTIME_MS] = end
            prefs[FocusPreferencesKeys.SESSION_TOTAL_SECONDS] = minutes * 60
        }
    }

    override suspend fun endSession() {
        dataStore.edit { prefs ->
            prefs[FocusPreferencesKeys.SESSION_END_ELAPSED_REALTIME_MS] = FocusPreferencesKeys.NO_SESSION
        }
    }

    override suspend fun setAppAllowed(packageName: String, allowed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[FocusPreferencesKeys.ALLOWED_PACKAGES] ?: emptySet()
            prefs[FocusPreferencesKeys.ALLOWED_PACKAGES] = if (allowed) current + packageName else current - packageName
        }
    }

    override suspend fun setSwitch(update: (FocusSwitches) -> FocusSwitches) {
        dataStore.edit { prefs ->
            val current = FocusSwitches(
                strict = prefs[FocusPreferencesKeys.SWITCH_STRICT] ?: true,
                holdToBreakOut = prefs[FocusPreferencesKeys.SWITCH_HOLD] ?: true,
                chime = prefs[FocusPreferencesKeys.SWITCH_CHIME] ?: false,
                keepClockVisible = prefs[FocusPreferencesKeys.SWITCH_CLOCK] ?: true,
            )
            val next = update(current)
            prefs[FocusPreferencesKeys.SWITCH_STRICT] = next.strict
            prefs[FocusPreferencesKeys.SWITCH_HOLD] = next.holdToBreakOut
            prefs[FocusPreferencesKeys.SWITCH_CHIME] = next.chime
            prefs[FocusPreferencesKeys.SWITCH_CLOCK] = next.keepClockVisible
        }
    }
}
