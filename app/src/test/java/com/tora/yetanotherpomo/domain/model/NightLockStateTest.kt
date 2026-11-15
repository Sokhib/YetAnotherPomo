package com.tora.yetanotherpomo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NightLockStateTest {

    private val instagram = "com.instagram.android"
    private val vk = "com.vkontakte.android"
    private val reddit = "com.reddit.frontpage"

    private val on = NightLockConfig(
        enabled = true,
        window = NightWindow(startMinute = 22 * 60, endMinute = 7 * 60),
        blockedPackages = setOf(instagram, vk),
    )
    private val armedTonight = NightLockState(config = on)

    private fun NightLockState.blocked(now: ClockReading): Set<String> =
        advance(now).session?.blockedPackages.orEmpty()

    // --- Schedule ---------------------------------------------------------------------------

    @Test
    fun `switched off never locks`() {
        val state = NightLockState(config = on.copy(enabled = false))
        assertNull(state.advance(at(23)).session)
    }

    @Test
    fun `locks inside the window and not outside it`() {
        assertNull(armedTonight.advance(at(21, 59)).session)

        val session = armedTonight.advance(at(22)).session
        assertNotNull(session)
        assertEquals(setOf(instagram, vk), session!!.blockedPackages)
        assertEquals(wall(7, day = 1), session.until.wallMs)
    }

    @Test
    fun `a running lock ends at the end of the window`() {
        val running = armedTonight.advance(at(23))
        assertNotNull(running.advance(at(6, 59, day = 1)).session)
        assertNull(running.advance(at(7, day = 1)).session)
    }

    @Test
    fun `advancing again within the same night changes nothing`() {
        val running = armedTonight.advance(at(23))
        assertEquals(running, running.advance(at(23, 1)))
        assertEquals(running, running.advance(at(3, day = 1)))
    }

    // --- Can't be undone in the moment ------------------------------------------------------

    @Test
    fun `winding the clock forward doesn't end a running lock`() {
        val running = armedTonight.advance(at(23))

        val woundForward = at(23, 30, wallSkewMs = 9 * HOUR_MS) // the wall clock now reads 08:30
        assertEquals(setOf(instagram, vk), running.blocked(woundForward))

        // It still ends on time by the monotonic clock.
        assertNull(running.advance(at(7, day = 1, wallSkewMs = 9 * HOUR_MS)).session)
    }

    @Test
    fun `changing the time zone forward doesn't end a running lock`() {
        val running = armedTonight.advance(at(23))
        val tokyo = java.util.TimeZone.getTimeZone("Asia/Tokyo") // 23:30 UTC is 08:30 there
        val travelled = at(23, 30).copy(timeZone = tokyo)

        assertNotNull(running.advance(travelled).session)
    }

    @Test
    fun `a lock survives a reboot`() {
        val running = armedTonight.advance(at(23))
        assertEquals(setOf(instagram, vk), running.blocked(afterReboot(1, day = 1)))
        assertNull(running.advance(afterReboot(7, day = 1)).session)
    }

    @Test
    fun `switching off during the lock waits until it is over`() {
        val running = armedTonight.advance(at(23))

        val edited = running.edit(on.copy(enabled = false), at(23, 5))

        assertFalse(edited.desired.enabled)
        assertTrue(edited.config.enabled)
        assertEquals(wall(7, day = 1), edited.pending!!.appliesAt.wallMs)
        assertEquals(setOf(instagram, vk), edited.blocked(at(3, day = 1)))

        val morning = edited.advance(at(7, day = 1))
        assertNull(morning.pending)
        assertFalse(morning.config.enabled)
        assertNull(morning.advance(at(23, day = 1)).session)
    }

    @Test
    fun `switching off before the lock still keeps tonight's`() {
        val edited = armedTonight.edit(on.copy(enabled = false), at(21, 55))

        assertEquals(setOf(instagram, vk), edited.blocked(at(22, 30)))
        assertNull(edited.advance(at(7, day = 1)).advance(at(22, 30, day = 1)).session)
    }

    @Test
    fun `a looser change made in the morning waits for the coming night too`() {
        val edited = armedTonight.edit(on.copy(blockedPackages = setOf(vk)), at(8, day = 1))

        assertEquals(wall(7, day = 2), edited.pending!!.appliesAt.wallMs)
        assertTrue(instagram in edited.blocked(at(23, day = 1)))
        assertFalse(instagram in edited.advance(at(7, day = 2)).blocked(at(23, day = 2)))
    }

    @Test
    fun `winding the clock forward doesn't apply a waiting change early`() {
        val edited = armedTonight.edit(on.copy(enabled = false), at(15))

        val woundForward = at(15, 5, wallSkewMs = 17 * HOUR_MS) // the wall clock reads 08:05 tomorrow
        assertNotNull(edited.advance(woundForward).pending)
    }

    @Test
    fun `shortening the window during the lock keeps tonight's end`() {
        val running = armedTonight.advance(at(23))
        val edited = running.edit(on.copy(window = NightWindow(22 * 60, 5 * 60)), at(23, 5))

        assertNotNull(edited.pending)
        assertNotNull(edited.advance(at(6, day = 1)).session)
    }

    // --- Stricter applies at once -----------------------------------------------------------

    @Test
    fun `switching on applies at once, also in the middle of the night`() {
        val off = NightLockState(config = on.copy(enabled = false))
        val edited = off.edit(on, at(23, 30))

        assertNull(edited.pending)
        assertEquals(setOf(instagram, vk), edited.session!!.blockedPackages)
    }

    @Test
    fun `blocking another app during the lock blocks it at once`() {
        val running = armedTonight.advance(at(23))
        val edited = running.edit(on.copy(blockedPackages = on.blockedPackages + reddit), at(23, 5))

        assertNull(edited.pending)
        assertTrue(reddit in edited.session!!.blockedPackages)
    }

    @Test
    fun `lengthening the window during the lock extends it at once`() {
        val running = armedTonight.advance(at(23))
        val edited = running.edit(on.copy(window = NightWindow(22 * 60, 8 * 60)), at(23, 5))

        assertNull(edited.pending)
        assertEquals(wall(8, day = 1), edited.session!!.until.wallMs)
    }

    @Test
    fun `an earlier start applies at once`() {
        val edited = armedTonight.edit(on.copy(window = NightWindow(21 * 60, 7 * 60)), at(21, 30))
        assertNotNull(edited.session)
    }

    @Test
    fun `a mixed change blocks the new app tonight and frees the old one tomorrow`() {
        val mixed = on.copy(blockedPackages = setOf(vk, reddit)) // drops Instagram, adds Reddit
        val edited = armedTonight.edit(mixed, at(15))

        assertNotNull(edited.pending)
        assertEquals(setOf(instagram, vk, reddit), edited.blocked(at(23)))

        val tomorrow = edited.advance(at(23)).advance(at(7, day = 1))
        assertEquals(mixed, tomorrow.config)
        assertEquals(setOf(vk, reddit), tomorrow.blocked(at(23, day = 1)))
    }

    @Test
    fun `a waiting change with a later window extends tonight's lock`() {
        val later = on.copy(window = NightWindow(23 * 60, 8 * 60)) // starts later, ends later
        val edited = armedTonight.edit(later, at(15))

        val tonight = edited.advance(at(22, 30))
        assertEquals(wall(7, day = 1), tonight.session!!.until.wallMs)

        val afterEleven = tonight.advance(at(23, 30))
        assertEquals(wall(8, day = 1), afterEleven.session!!.until.wallMs)
        assertNotNull(afterEleven.advance(at(7, 30, day = 1)).session)
        assertNull(afterEleven.advance(at(8, day = 1)).session)
    }

    // --- Network time correction -----------------------------------------------------------

    @Test
    fun `a lock armed on a wrong clock is corrected by network time`() {
        // Armed when the clock wrongly read 22:30; the real time was 14:30.
        val armedOnWrongClock = armedTonight.advance(at(14, 30, wallSkewMs = 8 * HOUR_MS))
        assertNotNull(armedOnWrongClock.session)

        assertNull(armedOnWrongClock.reanchor(at(14, 31)).session)
    }

    @Test
    fun `network time correction keeps every app tonight's lock blocks`() {
        val mixed = on.copy(blockedPackages = setOf(vk, reddit))
        val running = armedTonight.edit(mixed, at(15)).advance(at(23)).discardPending(at(23, 1))
        assertTrue(reddit in running.session!!.blockedPackages)

        val corrected = running.reanchor(at(23, 2))

        assertEquals(setOf(instagram, vk, reddit), corrected.session!!.blockedPackages)
        assertEquals(wall(7, day = 1), corrected.session!!.until.wallMs)
    }

    // --- Undo ------------------------------------------------------------------------------

    @Test
    fun `discarding a waiting change restores what is in force`() {
        val edited = armedTonight.edit(on.copy(enabled = false), at(15))
        val restored = edited.discardPending(at(15, 1))

        assertNull(restored.pending)
        assertEquals(on, restored.config)
        assertEquals(on, restored.desired)
    }

    @Test
    fun `asking for what is in force again needs no wait`() {
        val edited = armedTonight.edit(on.copy(enabled = false), at(15)).edit(on, at(15, 1))
        assertNull(edited.pending)
    }

    @Test
    fun `an unchanged edit is a no-op`() {
        val state = armedTonight.advance(at(15))
        assertEquals(state, state.edit(on, at(15)))
    }

    // --- Status ----------------------------------------------------------------------------

    @Test
    fun `next start covers the waiting config too`() {
        val earlier = on.copy(window = NightWindow(21 * 60, 6 * 60)) // mixed: earlier start, earlier end
        val edited = armedTonight.edit(earlier, at(15))

        assertEquals(wall(21), edited.nextStartWallMs(at(15)))
        assertNull(NightLockState(config = on.copy(enabled = false)).nextStartWallMs(at(15)))
    }

    @Test
    fun `default config is off with the social apps preselected`() {
        val default = NightLockConfig.Default
        assertFalse(default.enabled)
        assertEquals(NightWindow(22 * 60, 7 * 60), default.window)
        assertTrue(default.blockedPackages.containsAll(setOf(instagram, vk, reddit, "com.twitter.android")))
        assertSame(SocialApps.defaultBlocklist, default.blockedPackages)
    }
}
