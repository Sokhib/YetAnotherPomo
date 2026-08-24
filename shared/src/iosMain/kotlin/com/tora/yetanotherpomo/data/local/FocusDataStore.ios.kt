package com.tora.yetanotherpomo.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS has no `filesDir`; app-private storage is reached through [NSFileManager]. Documents is the
 * right container here - it is per-app, survives updates, and is included in device backups, which
 * matches how the Android store behaves.
 *
 * The `.preferences_pb` suffix is spelled out because on Android [preferencesDataStoreFile] appends
 * it for us and the two platforms should hold the same shape of file.
 */
@OptIn(ExperimentalForeignApi::class)
fun createFocusDataStore(): DataStore<Preferences> = createFocusDataStore {
    val documents: NSURL? = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    val path = requireNotNull(documents?.path) { "Could not resolve the iOS Documents directory" }
    "$path/$FOCUS_DATASTORE_NAME.preferences_pb"
}
