package com.tora.yetanotherpomo.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EscapePageMatcherTest {

    private val matcher = EscapePageMatcher(
        ownMarkers = listOf("Focus Lock", "com.tora.yetanotherpomo"),
        pageTitles = listOf("Accessibility", "Developer options", "Date & time"),
        pageSignatures = listOf("USB debugging", "Set time automatically"),
    )

    @Test
    fun `any text naming the app gives the page away`() {
        assertTrue(matcher.isEscapeText("Focus Lock"))
        assertTrue(matcher.isEscapeText("Focus Lock session blocking"))
        assertTrue(matcher.isEscapeText("Uninstall focus lock?"))
        assertTrue(matcher.isEscapeText("package:com.tora.yetanotherpomo"))
    }

    @Test
    fun `titles match only as titles, and only exactly`() {
        assertTrue(matcher.isEscapeTitle("Accessibility"))
        assertTrue(matcher.isEscapeTitle("  developer   options "))
        // Settings home lists "Accessibility" as a row: as plain text it must not block.
        assertFalse(matcher.isEscapeText("Accessibility"))
        assertFalse(matcher.isEscapeTitle("Accessibility shortcuts"))
        assertFalse(matcher.isEscapeTitle("Settings"))
    }

    @Test
    fun `signatures match exactly`() {
        assertTrue(matcher.isEscapeText("USB debugging"))
        assertTrue(matcher.isEscapeText("set time automatically"))
        assertFalse(matcher.isEscapeText("Debug mode when USB is connected"))
    }

    @Test
    fun `no-break spaces from translations still match`() {
        assertTrue(matcher.isEscapeTitle("Date & time"))
    }

    @Test
    fun `blank input never matches`() {
        assertFalse(matcher.isEscapeText(null))
        assertFalse(matcher.isEscapeText("   "))
        assertFalse(matcher.isEscapeTitle(""))
        assertFalse(EscapePageMatcher(listOf(""), listOf(""), listOf("")).isEscapeText("anything"))
    }
}
