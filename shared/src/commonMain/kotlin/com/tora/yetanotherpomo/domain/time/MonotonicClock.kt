package com.tora.yetanotherpomo.domain.time

/**
 * A clock that only ever moves forward, counts time the device spent asleep, and cannot be
 * changed from Settings. A focus timer needs all three properties: the wall clock fails the last
 * one (the session could be skipped by setting the date forward) and a plain uptime clock fails
 * the second (the countdown would stall in your pocket).
 *
 * Both platforms measure from the last boot, so a reading is only comparable to another reading
 * taken in the same uptime. [com.tora.yetanotherpomo.domain.model.FocusSession] already treats a
 * remaining time larger than the session length as proof of a reboot, which is exactly the case
 * this leaves open.
 *
 * Declared as an interface rather than a bare `expect fun` on purpose - see [systemMonotonicClock].
 */
fun interface MonotonicClock {
    fun elapsedRealtimeMs(): Long
}

/**
 * The real clock, supplied by whichever platform we compiled for.
 *
 * `expect` is used only for this leaf: the one thing that genuinely cannot be written in common
 * code. Everything above it depends on [MonotonicClock], the interface, so a test can hand a
 * repository a fake clock and step time by hand. That seam is why the pairing exists at all -
 * `expect`/`actual` is resolved by the compiler per target and has no runtime seam to substitute.
 */
expect fun systemMonotonicClock(): MonotonicClock
