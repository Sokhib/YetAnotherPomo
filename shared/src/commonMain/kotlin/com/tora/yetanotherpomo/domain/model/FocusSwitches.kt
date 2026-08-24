package com.tora.yetanotherpomo.domain.model

/** The Settings screen's four toggles, ported 1:1 from the reference prototype's `switches`. */
data class FocusSwitches(
    val strict: Boolean = true,
    val holdToBreakOut: Boolean = true,
    val chime: Boolean = false,
    val keepClockVisible: Boolean = true,
)
