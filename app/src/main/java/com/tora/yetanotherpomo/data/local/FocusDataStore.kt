package com.tora.yetanotherpomo.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.core.DataStore

/**
 * The one [DataStore] instance for this app's session/allowlist/settings state. DataStore
 * enforces a single active instance per file - [MainActivity] and [FocusAccessibilityService]
 * are different Android components and must never each open their own; both reach this same
 * object through [com.tora.yetanotherpomo.di.AppContainer].
 */
private const val FOCUS_DATASTORE_NAME = "focus_lock"

fun buildFocusDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create {
        context.applicationContext.preferencesDataStoreFile(FOCUS_DATASTORE_NAME)
    }

object FocusPreferencesKeys {
    val SESSION_MINUTES = intPreferencesKey("session_minutes")
    val SESSION_END_ELAPSED_REALTIME_MS = longPreferencesKey("session_end_elapsed_realtime_ms")
    val SESSION_TOTAL_SECONDS = intPreferencesKey("session_total_seconds")
    val ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
    val SWITCH_STRICT = booleanPreferencesKey("switch_strict")
    val SWITCH_HOLD = booleanPreferencesKey("switch_hold")
    val SWITCH_CHIME = booleanPreferencesKey("switch_chime")
    val SWITCH_CLOCK = booleanPreferencesKey("switch_clock")

    const val NO_SESSION = -1L
    const val DEFAULT_MINUTES = 25
    const val DEFAULT_HOLD_TO_END_MS = 1200
    const val DEFAULT_DIAL_MAX_MINUTES = 60
}

/**
 * Night lock's keys, in the same file as the focus keys because they live in the same single
 * DataStore instance. A [com.tora.yetanotherpomo.domain.model.Deadline] is three keys (wall,
 * elapsed, boot count); the pending change and the running lock exist exactly when their wall key
 * does.
 */
object NightLockPreferencesKeys {
    val ENABLED = booleanPreferencesKey("night_enabled")
    val START_MINUTE = intPreferencesKey("night_start_minute")
    val END_MINUTE = intPreferencesKey("night_end_minute")
    val BLOCKED = stringSetPreferencesKey("night_blocked")

    val PENDING_ENABLED = booleanPreferencesKey("night_pending_enabled")
    val PENDING_START_MINUTE = intPreferencesKey("night_pending_start_minute")
    val PENDING_END_MINUTE = intPreferencesKey("night_pending_end_minute")
    val PENDING_BLOCKED = stringSetPreferencesKey("night_pending_blocked")
    val PENDING_AT_WALL_MS = longPreferencesKey("night_pending_at_wall_ms")
    val PENDING_AT_ELAPSED_MS = longPreferencesKey("night_pending_at_elapsed_ms")
    val PENDING_AT_BOOT = intPreferencesKey("night_pending_at_boot")

    val SESSION_BLOCKED = stringSetPreferencesKey("night_session_blocked")
    val SESSION_UNTIL_WALL_MS = longPreferencesKey("night_session_until_wall_ms")
    val SESSION_UNTIL_ELAPSED_MS = longPreferencesKey("night_session_until_elapsed_ms")
    val SESSION_UNTIL_BOOT = intPreferencesKey("night_session_until_boot")
}
