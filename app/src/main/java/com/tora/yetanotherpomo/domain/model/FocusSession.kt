package com.tora.yetanotherpomo.domain.model

/**
 * A Pomodoro session. [endElapsedRealtimeMs] is `null` when no session is active. `running` and
 * `remainingSeconds` are deliberately *not* stored fields - they are derived so the ViewModel and
 * the AccessibilityService, which each read this independently, can never disagree about whether
 * a session is active.
 *
 * @param nowElapsedRealtimeMs the caller's current [android.os.SystemClock.elapsedRealtime]
 * reading, passed in rather than read internally so this stays a pure, testable data class.
 */
data class FocusSession(
    val minutes: Int,
    val endElapsedRealtimeMs: Long?,
    val totalSeconds: Int,
) {
    fun remainingSeconds(nowElapsedRealtimeMs: Long): Int {
        val end = endElapsedRealtimeMs ?: return 0
        val remainingMs = end - nowElapsedRealtimeMs
        // Clamped against totalSeconds: remaining can only shrink over time, so any value
        // exceeding the original duration proves elapsedRealtime reset under us (device reboot)
        // and the session must be treated as expired rather than resurrected.
        return (remainingMs / 1000).coerceIn(0, totalSeconds.toLong()).toInt()
    }

    fun isRunning(nowElapsedRealtimeMs: Long): Boolean =
        endElapsedRealtimeMs != null && remainingSeconds(nowElapsedRealtimeMs) > 0

    fun progressFraction(nowElapsedRealtimeMs: Long): Float {
        if (totalSeconds <= 0) return 0f
        val remaining = remainingSeconds(nowElapsedRealtimeMs)
        return (1f - remaining.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    }

    companion object {
        val Idle = FocusSession(minutes = 25, endElapsedRealtimeMs = null, totalSeconds = 25 * 60)
    }
}
