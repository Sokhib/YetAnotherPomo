package com.tora.yetanotherpomo.domain.model

/** A launchable app on the device. No Drawable here - icons are resolved lazily in the UI layer. */
data class InstalledApp(
    val packageName: String,
    val label: String,
)
