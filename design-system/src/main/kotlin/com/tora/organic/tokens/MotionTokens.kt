package com.tora.organic.tokens

/**
 * Durations and curves. Spring physics is deliberately absent: springs are described in
 * toolkit-specific units, so components that want one reach for their platform's spring directly.
 */
data class MotionTokens(
    /** Snap-back and other corrections the user should barely notice. */
    val quick: DurationToken,
    /** The default for state changes: toggles, colour transitions. */
    val standard: DurationToken,
    /** Transitions the user is meant to watch happen. */
    val deliberate: DurationToken,
    /** For progress that must track real elapsed time — hold-to-confirm, timers. */
    val linear: EasingToken,
    /** The default curve for state changes. */
    val emphasized: EasingToken,
)
