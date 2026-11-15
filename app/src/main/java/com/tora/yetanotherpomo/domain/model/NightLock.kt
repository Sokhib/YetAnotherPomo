package com.tora.yetanotherpomo.domain.model

/** What the person set up: whether Night lock runs, when, and which apps it closes. */
data class NightLockConfig(
    val enabled: Boolean,
    val window: NightWindow,
    val blockedPackages: Set<String>,
) {
    fun isActiveAt(minuteOfDay: Int): Boolean = enabled && window.contains(minuteOfDay)

    /**
     * True when switching from [other] to this config can't let anything through that [other]
     * would have blocked: still on, every app still blocked, every minute still covered. Such a
     * change applies at once; anything else waits until tonight's lock is over.
     */
    fun isAtLeastAsStrictAs(other: NightLockConfig): Boolean =
        !other.enabled ||
            (enabled && blockedPackages.containsAll(other.blockedPackages) && window.covers(other.window))

    companion object {
        val Default = NightLockConfig(
            enabled = false,
            window = NightWindow(startMinute = 22 * 60, endMinute = 7 * 60),
            blockedPackages = SocialApps.defaultBlocklist,
        )
    }
}

/**
 * A lock that has started and is being enforced. Its apps and end are frozen when it starts and
 * can only grow until [until] has passed, so nothing done during the night shortens it.
 */
data class NightSession(
    val blockedPackages: Set<String>,
    val until: Deadline,
)

/** A looser config the person asked for, held back until tonight's lock is over. */
data class PendingNightChange(
    val config: NightLockConfig,
    val appliesAt: Deadline,
)

/**
 * Everything Night lock persists, and the only place its rules live.
 *
 * Rules:
 * - A lock starts when the wall clock enters the window of [config] or of a [pending] config.
 * - Once started, it runs until its [NightSession.until] has passed on both clocks.
 * - Stricter edits apply at once and widen a running lock. Looser edits wait in [pending] until
 *   tonight's lock ends; until then both configs are enforced, so the loosening never reaches
 *   tonight while any tightening in the same edit does.
 *
 * Every function is pure: callers pass the time in, and persist the result if it changed.
 */
data class NightLockState(
    val config: NightLockConfig = NightLockConfig.Default,
    val pending: PendingNightChange? = null,
    val session: NightSession? = null,
) {
    /** What the person last asked for, and what the editor shows: the pending config if any. */
    val desired: NightLockConfig get() = pending?.config ?: config

    /** Brings the state up to [now]: applies a due change, ends a finished lock, starts a due one. */
    fun advance(now: ClockReading): NightLockState {
        var config = config
        var pending = pending
        var session = session

        if (pending != null && pending.appliesAt.hasPassed(now)) {
            config = pending.config
            pending = null
        }
        if (session != null && session.until.hasPassed(now)) {
            session = null
        }
        val minute = now.minuteOfDay
        for (source in listOfNotNull(config, pending?.config)) {
            if (source.isActiveAt(minute)) session = session.absorb(source, now)
        }
        return NightLockState(config, pending, session)
    }

    /** Applies the person's edit, [desired], under the stricter-now, looser-later rule. */
    fun edit(desired: NightLockConfig, now: ClockReading): NightLockState {
        val current = advance(now)
        if (desired.isAtLeastAsStrictAs(current.config)) {
            return NightLockState(config = desired, pending = null, session = current.session).advance(now)
        }
        // Wait for whichever ends later: the lock running now, or the active config's tonight.
        val tonightEnd = current.config.window.endOfCurrentOrNextOccurrence(now)?.let { Deadline.at(it, now) }
        val appliesAt = listOfNotNull(current.session?.until, tonightEnd).reduceOrNull(::later)
            ?: return NightLockState(config = desired, pending = null, session = current.session).advance(now)
        return current.copy(pending = PendingNightChange(desired, appliesAt)).advance(now)
    }

    /**
     * Re-derives the running lock's end after the network corrected the clock, for a lock that
     * was armed while the clock was wrong. Its end can move either way, and it stops if the
     * corrected time is outside every window; while it runs, it keeps every app it already blocks.
     */
    fun reanchor(now: ClockReading): NightLockState {
        val fresh = copy(session = null).advance(now)
        val previous = session ?: return fresh
        val rebuilt = fresh.session ?: return fresh
        return fresh.copy(session = rebuilt.copy(blockedPackages = rebuilt.blockedPackages + previous.blockedPackages))
    }

    /** Drops a waiting change. Keeping what's in force is never looser, so it applies at once. */
    fun discardPending(now: ClockReading): NightLockState {
        val current = advance(now)
        return current.edit(current.config, now)
    }

    /**
     * Wall-clock start of the next lock that isn't running yet, across [config] and [pending].
     * Null when neither is switched on.
     */
    fun nextStartWallMs(now: ClockReading): Long? =
        listOfNotNull(config, pending?.config)
            .filter { it.enabled }
            .mapNotNull { it.window.nextStart(now) }
            .minOrNull()

    private fun NightSession?.absorb(source: NightLockConfig, now: ClockReading): NightSession {
        val end = Deadline.at(source.window.endOfOccurrenceContaining(now), now)
        if (this == null) return NightSession(source.blockedPackages, end)
        val blocked = if (blockedPackages.containsAll(source.blockedPackages)) {
            blockedPackages
        } else {
            blockedPackages + source.blockedPackages
        }
        return NightSession(blocked, later(until, end))
    }
}
