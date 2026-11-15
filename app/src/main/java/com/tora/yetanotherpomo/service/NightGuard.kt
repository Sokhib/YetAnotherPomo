package com.tora.yetanotherpomo.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.tora.yetanotherpomo.R
import java.util.Locale

private const val TAG = "NightGuard"

/** Enough for any Settings page; stops a pathological window from stalling the main thread. */
private const val MAX_SCANNED_NODES = 600

/**
 * Night lock's self-protection: which packages host the pages that could switch it off, and
 * whether the page on screen is one of them.
 *
 * Settings shows most pages inside one shared activity (`SubSettings`), so the package or
 * activity alone can't tell Developer options from Display. The page is identified from, in
 * order: its activity name when it has its own, its window title, and finally its visible text.
 *
 * @param guardedPackages Settings, the uninstall dialog's package installer, and whatever else
 * resolves the escape intents on this phone.
 * @param wholePackages packages that are nothing but escape pages: a separate accessibility
 * settings app (Samsung ships one). Every window of theirs is blocked during the lock.
 */
class NightGuard private constructor(
    val guardedPackages: Set<String>,
    private val wholePackages: Set<String>,
    private val matcher: EscapePageMatcher,
) {

    /**
     * @param className the activity (or window) class from the window-state event, if any.
     * @param eventTexts the window-state event's text: an activity's title, a dialog's lines.
     * @param findRoot the page's window content, or null when it can't be read. Only called when
     * the cheaper signals didn't decide, because finding it is a cross-process call.
     * @param isUninstallProtectionOn asked only on the device-admin activation page.
     */
    fun isEscapePage(
        packageName: String,
        className: String?,
        eventTexts: List<String>,
        findRoot: () -> AccessibilityNodeInfo?,
        isUninstallProtectionOn: () -> Boolean,
    ): Boolean {
        if (packageName in wholePackages) return true

        val activity = className?.lowercase(Locale.ROOT)
        if (activity != null) {
            // Pages that can only add protection stay usable, so it can be set up at night too.
            if (ALWAYS_OPEN_ACTIVITY_HINTS.any { it in activity }) return false
            if (DEVICE_ADMIN_ADD_HINT in activity && !isUninstallProtectionOn()) return false
            if (ESCAPE_ACTIVITY_HINTS.any { it in activity }) return true
        }

        if (eventTexts.any { matcher.isEscapeTitle(it) || matcher.isEscapeText(it) }) return true

        return try {
            val root = findRoot() ?: return false
            matcher.isEscapeTitle(root.window?.title) || containsEscapeText(root)
        } catch (e: RuntimeException) {
            // A window can vanish mid-read. Treat it as unknown; the next event reads it again.
            Log.w(TAG, "Couldn't read $packageName", e)
            false
        }
    }

    /** Breadth-first, so the title and the top of the page are read before deep list rows. */
    private fun containsEscapeText(root: AccessibilityNodeInfo): Boolean {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.addLast(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_SCANNED_NODES) {
            val node = queue.removeFirst()
            visited++
            if (matcher.isEscapeText(node.text) || matcher.isEscapeText(node.contentDescription)) return true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && matcher.isEscapeTitle(node.paneTitle)) return true
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(queue::addLast)
            }
        }
        return false
    }

    companion object {
        val Empty = NightGuard(emptySet(), emptySet(), EscapePageMatcher(emptyList(), emptyList(), emptyList()))

        /** Lowercase fragments of AOSP activity names that each host exactly one escape page. */
        private val ESCAPE_ACTIVITY_HINTS = listOf(
            "accessibilitysettings", // Settings$AccessibilitySettingsActivity
            "accessibilitydetails", // AccessibilityDetailsSettingsActivity (one service's page)
            "deviceadminsettings", // Settings$DeviceAdminSettingsActivity
            "deviceadminadd", // DeviceAdminAdd while active: the "Deactivate" page
            "developmentsettings", // Settings$DevelopmentSettingsDashboardActivity
            "datetimesettings", // Settings$DateTimeSettingsActivity
        )

        /** Activating device admin only adds protection; reachable while it is still off. */
        private const val DEVICE_ADMIN_ADD_HINT = "deviceadminadd"

        /** Grant-only dialogs that mention Focus Lock by name. */
        private val ALWAYS_OPEN_ACTIVITY_HINTS = listOf(
            "requestignorebatteryoptimizations",
        )

        /** Settings string resources, resolved in the phone's language. */
        private val TITLE_RESOURCES = listOf(
            "accessibility_settings",
            "accessibility_settings_title",
            "manage_device_admin",
            "device_admin_settings_title",
            "development_settings_title",
            "date_and_time",
            "date_and_time_settings_title",
        )
        private val SIGNATURE_RESOURCES = listOf(
            "enable_adb", // USB debugging
            "enable_adb_wireless", // Wireless debugging
            "keep_screen_on", // Stay awake
            "oem_unlock_enable", // OEM unlocking
            "date_time_auto", // Set time automatically
            "zone_auto_title", // Set time zone automatically
        )

        /** For phones whose Settings renamed the resources; English is the most common fallback. */
        private val FALLBACK_TITLES = listOf(
            "Accessibility",
            "Device admin apps",
            "Device administrators",
            "Device admin",
            "Developer options",
            "Date & time",
            "Date and time",
        )
        private val FALLBACK_SIGNATURES = listOf(
            "USB debugging",
            "Wireless debugging",
            "Stay awake",
            "OEM unlocking",
            "Use developer options",
            "Set time automatically",
            "Automatic date & time",
            "Use network-provided time",
            "Set time zone automatically",
            "Automatic time zone",
            "Use network-provided time zone",
        )

        private val FALLBACK_GUARDED_PACKAGES = listOf(
            "com.android.settings",
            "com.android.packageinstaller",
            "com.google.android.packageinstaller",
        )

        /** Resolver/chooser windows report "android"; never treat the OS itself as a page. */
        private const val ANDROID_PACKAGE = "android"

        fun compute(context: Context, policy: SystemPackagePolicy): NightGuard {
            val pm = context.packageManager
            val ownPackageUri = Uri.fromParts("package", context.packageName, null)
            fun resolve(intent: Intent): String? = intent.resolveActivity(pm)?.packageName

            val mainSettings = resolve(Intent(Settings.ACTION_SETTINGS))
            val accessibilitySettings = resolve(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            val settingsPackages = listOfNotNull(
                mainSettings,
                accessibilitySettings,
                resolve(Intent(Settings.ACTION_DATE_SETTINGS)),
                resolve(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)),
                resolve(Intent(Settings.ACTION_SECURITY_SETTINGS)),
                resolve(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, ownPackageUri)),
            )
            @Suppress("DEPRECATION") // ACTION_UNINSTALL_PACKAGE: still what launchers send.
            val installerPackages = listOfNotNull(
                resolve(Intent(Intent.ACTION_DELETE, ownPackageUri)),
                resolve(Intent(Intent.ACTION_UNINSTALL_PACKAGE, ownPackageUri)),
            )

            // Never guard what must always work: Home, keyboards, System UI, this app.
            val neverGuarded = policy.transparent + policy.home + ANDROID_PACKAGE
            val guarded = (settingsPackages + installerPackages + FALLBACK_GUARDED_PACKAGES).toSet() - neverGuarded
            val whole = setOfNotNull(accessibilitySettings?.takeIf { it != mainSettings }) - neverGuarded

            val ownMarkers = listOf(
                context.packageName,
                context.applicationInfo.loadLabel(pm).toString(),
                context.getString(R.string.accessibility_service_label),
                context.getString(R.string.night_lock_admin_label),
            )
            val matcher = EscapePageMatcher(
                ownMarkers = ownMarkers,
                pageTitles = FALLBACK_TITLES + settingsStrings(pm, settingsPackages, TITLE_RESOURCES),
                pageSignatures = FALLBACK_SIGNATURES + settingsStrings(pm, settingsPackages, SIGNATURE_RESOURCES),
            )
            return NightGuard(guarded, whole, matcher)
        }

        /** Reads Settings' own strings, so the matcher works in whatever language the phone uses. */
        @SuppressLint("DiscouragedApi") // Another app's resources can only be looked up by name.
        private fun settingsStrings(pm: PackageManager, packages: List<String>, names: List<String>): List<String> =
            packages.distinct().flatMap { pkg ->
                val resources = try {
                    pm.getResourcesForApplication(pkg)
                } catch (e: PackageManager.NameNotFoundException) {
                    return@flatMap emptyList()
                }
                names.mapNotNull { name ->
                    val id = resources.getIdentifier(name, "string", pkg)
                    if (id == 0) null else runCatching { resources.getString(id) }.getOrNull()
                }
            }
    }
}
