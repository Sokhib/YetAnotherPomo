package com.tora.yetanotherpomo.ui

import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.domain.model.InstalledApp

data class FocusUiState(
    val minutes: Int,
    val remainingSeconds: Int,
    val totalSeconds: Int,
    val isRunning: Boolean,
    val progressFraction: Float,
    val allowedPackages: Set<String>,
    val installedApps: List<InstalledApp>,
    val switches: FocusSwitches,
    val isAccessibilityGranted: Boolean,
    val dialMaxMinutes: Int,
    val holdToEndMs: Int,
) {
    companion object {
        val Initial = FocusUiState(
            minutes = 25,
            remainingSeconds = 25 * 60,
            totalSeconds = 25 * 60,
            isRunning = false,
            progressFraction = 0f,
            allowedPackages = emptySet(),
            installedApps = emptyList(),
            switches = FocusSwitches(),
            isAccessibilityGranted = false,
            dialMaxMinutes = 60,
            holdToEndMs = 1200,
        )
    }
}
