package com.tora.yetanotherpomo.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.mutableLongStateOf
import com.tora.organic.OrganicDesignSystem
import com.tora.yetanotherpomo.FocusLockApplication
import com.tora.yetanotherpomo.MainActivity
import com.tora.yetanotherpomo.domain.model.FocusSession
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.ui.components.DockEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** How often the overlay's countdown is re-read, and how often session expiry is re-checked. */
private const val TICK_MS = 1000L

/**
 * How long after the overlay appears an "allowed app is foreground" reading is treated as
 * launch-transition noise rather than a real switch. See [FocusAccessibilityService.isNoise].
 */
private const val SHOW_SETTLE_MS = 1000L

/** Dock entries shown on the overlay; the rest of the allowlist is reachable from Home. */
private const val DOCK_SLOTS = 4

/**
 * Detects foreground-app switches via [TYPE_WINDOW_STATE_CHANGED][AccessibilityEvent] and blocks
 * non-allowlisted apps during a strict session with a [TYPE_ACCESSIBILITY_OVERLAY] window - no
 * `PACKAGE_USAGE_STATS` or `SYSTEM_ALERT_WINDOW` permission needed. `canRetrieveWindowContent` is
 * false in accessibility_service_config.xml: only `event.packageName` is read.
 */
class FocusAccessibilityService : AccessibilityService() {

    private data class Snapshot(
        val session: FocusSession,
        val allowed: Set<String>,
        val switches: FocusSwitches,
    ) {
        companion object {
            val Empty = Snapshot(FocusSession.Idle, emptySet(), FocusSwitches())
        }
    }

    // Main, not Default: evaluate()/showOverlay()/hideOverlay() below call into WindowManager
    // and the overlay's Compose/View lifecycle, which must only ever be touched from the main
    // thread - same thread onAccessibilityEvent is already dispatched on.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: FocusRepository
    private lateinit var overlayController: OverlayController
    private var policy = SystemPackagePolicy(emptySet(), emptySet())

    /** Read synchronously on the event-dispatch thread - never a suspending DataStore read there. */
    @Volatile
    private var snapshot: Snapshot = Snapshot.Empty

    /**
     * The app genuinely occupying the foreground - *not* simply the last package to emit a window
     * event. Only non-[transparent][SystemPackagePolicy.transparent], non-noise events move it;
     * everything else layers over the real foreground app without replacing it.
     */
    private var foregroundPackage: String? = null

    /** Resolved once per package: label lookup is a PackageManager IPC plus a cross-APK resource read. */
    private val labelCache = HashMap<String, String>()

    /** Rebuilt whenever the allowlist changes, so showing the overlay never waits on PackageManager. */
    private var dockApps: List<DockEntry> = emptyList()

    private var tickerJob: Job? = null
    private val overlayNowState = mutableLongStateOf(SystemClock.elapsedRealtime())

    /** When the overlay currently on screen went up; drives the [isNoise] settle window. */
    private var lastShowAtMs = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val container = (application as FocusLockApplication).container
        repository = container.focusRepository
        overlayController = OverlayController(this)
        policy = SystemPackagePolicy.compute(this)

        serviceScope.launch {
            combine(repository.sessionFlow, repository.allowedFlow, repository.switchesFlow) { session, allowed, switches ->
                Snapshot(session, allowed, switches)
            }.distinctUntilChanged().collect { snap ->
                val allowlistChanged = snap.allowed != snapshot.allowed
                snapshot = snap
                if (allowlistChanged || dockApps.isEmpty()) {
                    dockApps = snap.allowed.take(DOCK_SLOTS).mapIndexed { index, pkg ->
                        DockEntry(packageName = pkg, label = resolveLabel(pkg), toneIndex = index)
                    }
                }
                foregroundPackage?.let(::evaluate)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // System UI, keyboards and our own overlay window all raise WINDOW_STATE_CHANGED while
        // the app underneath stays exactly where it is. Recording one as the new foreground app
        // is what caused the flicker: they are all allowlisted, so the very next evaluate() tore
        // the overlay down, re-exposed the blocked app, which re-fired its own event, which
        // re-showed the overlay - forever. They are never a foreground switch; drop them.
        if (pkg in policy.transparent) return

        if (pkg == foregroundPackage) return
        if (isNoise(pkg)) return

        foregroundPackage = pkg
        evaluate(pkg)
    }

    /**
     * True for an allowed-app event arriving in the first [SHOW_SETTLE_MS] after the overlay went
     * up. The launcher (and other allowlisted system surfaces) keep emitting window-state events
     * for a beat as the app-open animation finishes, well after the blocked app is already
     * foreground. Such an event is *discarded*, not merely deferred - leaving [foregroundPackage]
     * pinned to the blocked app - because a deferred hide would simply fire on the next tick and
     * restart the flicker.
     */
    private fun isNoise(packageName: String): Boolean =
        overlayController.isShowing &&
            SystemClock.elapsedRealtime() - lastShowAtMs < SHOW_SETTLE_MS &&
            !isBlocked(packageName, SystemClock.elapsedRealtime())

    private fun isBlocked(packageName: String, nowMs: Long): Boolean {
        val snap = snapshot
        return snap.session.isRunning(nowMs) &&
            snap.switches.strict &&
            packageName !in snap.allowed &&
            packageName !in policy.alwaysAllowed
    }

    private fun evaluate(packageName: String) {
        val now = SystemClock.elapsedRealtime()
        if (isBlocked(packageName, now)) showOverlay(packageName) else hideOverlay()
    }

    private fun showOverlay(blockedPackage: String) {
        if (overlayController.isShowing) return
        val now = SystemClock.elapsedRealtime()
        lastShowAtMs = now
        overlayNowState.longValue = now
        val blockedLabel = resolveLabel(blockedPackage)
        val entries = dockApps
        val holdMs = repository.holdToEndMs

        // Same token BlockedOverlayContent fills itself with, read non-composably so the window
        // is opaque from frame one rather than from first composition.
        overlayController.show(backgroundColor = OrganicDesignSystem.colors.lockedSurface.argb.toInt()) {
            val tick = overlayNowState.longValue
            BlockedOverlayContent(
                blockedAppLabel = blockedLabel,
                remainingSeconds = snapshot.session.remainingSeconds(tick),
                totalSeconds = snapshot.session.totalSeconds,
                dockApps = entries,
                holdToEndMs = holdMs,
                onBackToFocus = { returnToApp() },
                onEndSession = { serviceScope.launch { repository.endSession() } },
            )
        }

        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(TICK_MS)
                overlayNowState.longValue = SystemClock.elapsedRealtime()
                // Deliberately the package the overlay was raised for, not whatever the latest
                // event mentioned: the tick exists to notice the session expiring underneath a
                // still-blocked app, never to re-decide which app is in front.
                evaluate(blockedPackage)
            }
        }
    }

    private fun hideOverlay() {
        tickerJob?.cancel()
        tickerJob = null
        overlayController.hide()
    }

    private fun returnToApp() {
        hideOverlay()
        // Our own package is transparent, so returning here raises no event that would move
        // foregroundPackage - clear it explicitly, otherwise re-opening the same blocked app
        // would be deduplicated away and never blocked.
        foregroundPackage = null
        startActivity(
            Intent(this, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP,
            ),
        )
    }

    /** Cached for the life of the service; a label change needs a reinstall, which rebinds us anyway. */
    private fun resolveLabel(packageName: String): String = labelCache.getOrPut(packageName) {
        runCatching {
            val info = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    override fun onInterrupt() {
        hideOverlay()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hideOverlay()
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        hideOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}
