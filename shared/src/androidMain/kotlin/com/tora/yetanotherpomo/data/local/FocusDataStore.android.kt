package com.tora.yetanotherpomo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile

/**
 * Deliberately delegates to [preferencesDataStoreFile] instead of assembling the path by hand.
 *
 * The old code called `PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(...) }`,
 * and that helper puts the file at a specific spot under `filesDir` with a specific extension. Any
 * app already installed has real sessions and allowlists sitting at exactly that path - hardcoding
 * a path that merely *looks* right would silently hand every existing user an empty database. So we
 * keep asking the platform where the file goes and only pass the answer across.
 */
fun createFocusDataStore(context: Context): DataStore<Preferences> =
    createFocusDataStore {
        context.applicationContext.preferencesDataStoreFile(FOCUS_DATASTORE_NAME).absolutePath
    }
