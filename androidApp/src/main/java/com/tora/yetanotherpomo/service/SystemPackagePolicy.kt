package com.tora.yetanotherpomo.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

/**
 * How [FocusAccessibilityService] classifies a package that reports itself as foreground.
 *
 * @param alwaysAllowed packages that must remain reachable no matter what, so a strict session
 * can never fully lock the user out of their device: our own app, the default launcher (Home
 * button always escapes), and the Settings app (resolved dynamically for both entry points,
 * since some OEMs rename the package - hardcoding "com.android.settings" would silently fail on
 * those).
 *
 * @param transparent packages whose windows layer *over* whatever app is really in the
 * foreground rather than replacing it: system UI, every enabled keyboard, and our own overlay.
 * Their `TYPE_WINDOW_STATE_CHANGED` events are dropped outright - see the comment in
 * [FocusAccessibilityService.onAccessibilityEvent] for why reading them as a foreground switch
 * produces a visible show/hide flicker. [transparent] is a subset of [alwaysAllowed]: dropping
 * the event is the primary defence, allowlisting is the backstop if some device routes one of
 * these through a path we don't drop.
 */
data class SystemPackagePolicy(
    val alwaysAllowed: Set<String>,
    val transparent: Set<String>,
) {
    companion object {
        /**
         * System UI is hardcoded as the one deliberate exception to dynamic resolution: it has
         * no launchable intent to resolve by, but is AOSP-stable across virtually all shipping
         * devices; if it doesn't match on some device, Home and Settings remain the guaranteed
         * escape hatches.
         */
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        fun compute(context: Context): SystemPackagePolicy {
            val pm = context.packageManager

            // Every enabled keyboard is transparent, not just the current default, so switching
            // keyboards mid-session doesn't require recomputing this set. An IME has no launcher
            // activity, so it can never appear in the allowlist picker, yet its window reports
            // its own package as foreground the moment a text field is focused.
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val transparent = buildSet {
                add(context.packageName)
                add(SYSTEM_UI_PACKAGE)
                imm.enabledInputMethodList.forEach { add(it.packageName) }
            }

            val alwaysAllowed = buildSet {
                addAll(transparent)

                val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                pm.resolveActivity(homeIntent, 0)?.activityInfo?.packageName?.let(::add)

                Intent(Settings.ACTION_SETTINGS).resolveActivity(pm)?.packageName?.let(::add)
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).resolveActivity(pm)?.packageName?.let(::add)
            }

            return SystemPackagePolicy(alwaysAllowed = alwaysAllowed, transparent = transparent)
        }
    }
}
