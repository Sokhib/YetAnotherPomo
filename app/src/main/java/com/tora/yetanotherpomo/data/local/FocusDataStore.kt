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
