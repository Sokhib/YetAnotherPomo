package com.tora.yetanotherpomo.service

import java.util.Locale

/**
 * Decides from on-screen text whether a Settings-like window is one of the pages that could switch
 * Night lock off. Pure string logic, so it is unit-tested without a device; [NightGuard] feeds it
 * the window's title and visible text.
 *
 * @param ownMarkers this app's own names (app label, service label, package name). Any text
 * *containing* one gives the page away: App info, the Accessibility entry, Device admin apps, the
 * uninstall dialog and battery pages all show Focus Lock by name.
 * @param pageTitles titles of the escape pages that don't name this app: Accessibility, Device
 * admin apps, Developer options, Date & time. Matched *exactly* and only against titles, because
 * the same words are ordinary rows on harmless parent pages (Settings home lists "Accessibility").
 * @param pageSignatures row labels found only on an escape page ("USB debugging", "Set time
 * automatically"), matched exactly against any visible text.
 */
class EscapePageMatcher(
    ownMarkers: Collection<String>,
    pageTitles: Collection<String>,
    pageSignatures: Collection<String>,
) {
    private val ownMarkers = ownMarkers.normalizedSet()
    private val pageTitles = pageTitles.normalizedSet()
    private val pageSignatures = pageSignatures.normalizedSet()

    fun isEscapeTitle(title: CharSequence?): Boolean =
        !title.isNullOrBlank() && normalize(title) in pageTitles

    fun isEscapeText(text: CharSequence?): Boolean {
        if (text.isNullOrBlank()) return false
        val normalized = normalize(text)
        return normalized in pageSignatures || ownMarkers.any { it in normalized }
    }

    companion object {
        // \s alone misses the no-break spaces some translations put inside "Date & time".
        private val WHITESPACE = Regex("[\\s\\u00A0\\u2007\\u202F]+")

        fun normalize(text: CharSequence): String =
            text.toString().replace(WHITESPACE, " ").trim().lowercase(Locale.ROOT)

        private fun Collection<String>.normalizedSet(): Set<String> =
            mapTo(HashSet()) { normalize(it) }.apply { remove("") }
    }
}
