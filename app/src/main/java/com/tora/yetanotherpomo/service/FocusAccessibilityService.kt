package com.tora.yetanotherpomo.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.text.format.DateFormat
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.tora.organic.OrganicDesignSystem
import com.tora.yetanotherpomo.FocusLockApplication
import com.tora.yetanotherpomo.MainActivity
import com.tora.yetanotherpomo.data.time.SystemTimeSettings
import com.tora.yetanotherpomo.domain.model.ClockReading
import com.tora.yetanotherpomo.domain.model.FocusSession
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.domain.model.NightLockState
import com.tora.yetanotherpomo.domain.model.NightSession
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.domain.repository.NightClock
import com.tora.yetanotherpomo.domain.repository.NightLockRepository
import com.tora.yetanotherpomo.domain.repository.NightProtectionChecker
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
import java.util.Date

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
 * Let the overlay window actually relinquish input focus before injecting Back. Removing a
 * window and recomputing focus is asynchronous inside WindowManager, and a Back injected too
 * early is delivered straight back to the window we just took down.
 */
private const val FOCUS_RELEASE_MS = 60L

/** How long to give the injected Back to actually move us off the blocked app. */
private const val BACK_SETTLE_MS = 600L

/** Upper bound on an in-flight exit; blocking resumes unconditionally once it lapses. */
private const val EXIT_GRACE_MS = 2000L

/** Settings pages fill in after they open and scroll in more rows; re-read one at most this often. */
private const val PAGE_RESCAN_MS = 150L

/** What accessibility_service_config.xml subscribes to, all day. */
private const val BASE_EVENT_TYPES = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

/**
 * Added only while Night lock runs, to keep reading a Settings page as it fills in and scrolls.
 * Content changes fire constantly in every app, so they are not worth receiving the rest of the day.
 */
private const val PAGE_EVENT_TYPES =
    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_VIEW_SCROLLED

/** Why the app in front is blocked. */
private sealed interface Block {
    /** Off the allowlist during a focus session; ending the session unblocks it. */
    data object Focus : Block

    /** On the Night lock blocklist; nothing unblocks it before [untilWallMs]. */
    data class NightApp(val untilWallMs: Long) : Block

    /** A Settings page that could switch Night lock off; see [NightGuard]. */
    data class NightPage(val untilWallMs: Long) : Block
}

private data class OverlayModel(
    val label: String,
    val block: Block,
    val dock: List<DockEntry>,
)

/**
 * Detects foreground-app switches via [TYPE_WINDOW_STATE_CHANGED][AccessibilityEvent] and covers
 * blocked apps with a [TYPE_ACCESSIBILITY_OVERLAY] window - no `PACKAGE_USAGE_STATS` or
 * `SYSTEM_ALERT_WINDOW` permission needed. Two things block an app:
 *
 * - A strict focus session blocks everything off the allowlist. Only `event.packageName` is read.
 * - Night lock blocks its blocklist, and also the Settings pages that could switch it off. Those
 *   pages are told apart by their text, which is the only reason `canRetrieveWindowContent` is on;
 *   window content is read only for those packages, and only while Night lock runs.
 */
class FocusAccessibilityService : AccessibilityService() {

    private data class Snapshot(
        val session: FocusSession,
        val allowed: Set<String>,
        val switches: FocusSwitches,
        val night: NightLockState,
    ) {
        companion object {
            val Empty = Snapshot(FocusSession.Idle, emptySet(), FocusSwitches(), NightLockState())
        }
    }

    // Main, not Default: evaluate()/showOverlay()/hideOverlay() below call into WindowManager
    // and the overlay's Compose/View lifecycle, which must only ever be touched from the main
    // thread - same thread onAccessibilityEvent is already dispatched on.
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var repository: FocusRepository
    private lateinit var nightRepository: NightLockRepository
    private lateinit var nightClock: NightClock
    private lateinit var protectionChecker: NightProtectionChecker
    private lateinit var overlayController: OverlayController
    private var policy = SystemPackagePolicy.Empty
    private var guard = NightGuard.Empty

    /** Read synchronously on the event-dispatch thread - never a suspending DataStore read there. */
    @Volatile
    private var snapshot: Snapshot = Snapshot.Empty

    /**
     * The app genuinely occupying the foreground - *not* simply the last package to emit a window
     * event. Only non-[transparent][SystemPackagePolicy.transparent], non-noise events move it;
     * everything else layers over the real foreground app without replacing it.
     */
    private var foregroundPackage: String? = null

    /**
     * The page in front, kept for re-reading it while Night lock runs. Copied out of the event,
     * which the platform recycles as soon as [onAccessibilityEvent] returns.
     */
    private var pageClassName: String? = null
    private var pageTexts: List<String> = emptyList()

    /** Whether that page is one of [NightGuard]'s; null until read during a lock. */
    private var pageIsEscape: Boolean? = null
    private var pageScanJob: Job? = null

    /** Resolved once per package: label lookup is a PackageManager IPC plus a cross-APK resource read. */
    private val labelCache = HashMap<String, String>()

    /** Rebuilt whenever the allowlist changes, so showing the overlay never waits on PackageManager. */
    private var dockApps: List<DockEntry> = emptyList()

    private var tickerJob: Job? = null
    private val overlayNowState = mutableLongStateOf(SystemClock.elapsedRealtime())

    /** What the overlay shows; Compose state, so a change of reason updates it in place. */
    private val overlayModel = mutableStateOf<OverlayModel?>(null)

    /** When the overlay currently on screen went up; drives the [isNoise] settle window. */
    private var lastShowAtMs = 0L

    /**
     * Non-zero while a user-initiated exit ([leaveBlocked]) is in flight. Cleared the moment
     * an allowed app reaches the foreground, which is what "the exit landed" actually means.
     */
    private var exitingUntilMs = 0L

    private val keyguardManager by lazy { getSystemService(KeyguardManager::class.java) }

    private var nightRefreshJob: Job? = null
    private var pageEventsSubscribed = false
    private var systemEventsRegistered = false

    /**
     * Moments to re-decide the app in front without waiting for it to raise an event of its own:
     * Night lock can start while a blocked app is already open, and a clock change moves it.
     */
    private val systemEvents = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIME_CHANGED ->
                    if (SystemTimeSettings.isNetworkTimeOn(context)) reanchorNightLock()
                Intent.ACTION_TIMEZONE_CHANGED ->
                    if (SystemTimeSettings.isNetworkTimeZoneOn(context)) reanchorNightLock()
                Intent.ACTION_LOCALE_CHANGED ->
                    guard = NightGuard.compute(this@FocusAccessibilityService, policy)
            }
            recheckForeground()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val container = (application as FocusLockApplication).container
        repository = container.focusRepository
        nightRepository = container.nightLockRepository
        nightClock = container.nightClock
        protectionChecker = container.nightProtectionChecker
        overlayController = OverlayController(this)
        policy = SystemPackagePolicy.compute(this)
        guard = NightGuard.compute(this, policy)
        registerSystemEvents()

        serviceScope.launch {
            combine(
                repository.sessionFlow,
                repository.allowedFlow,
                repository.switchesFlow,
                nightRepository.stateFlow,
            ) { session, allowed, switches, night ->
                Snapshot(session, allowed, switches, night)
            }.distinctUntilChanged().collect { snap ->
                val allowlistChanged = snap.allowed != snapshot.allowed
                snapshot = snap
                if (allowlistChanged || dockApps.isEmpty()) {
                    dockApps = snap.allowed.take(DOCK_SLOTS).mapIndexed { index, pkg ->
                        DockEntry(packageName = pkg, label = resolveLabel(pkg), toneIndex = index)
                    }
                }
                evaluate()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> onWindowStateChanged(pkg, event)
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> onPageContentChanged(pkg)
            else -> Unit // Not subscribed to anything else.
        }
    }

    private fun onWindowStateChanged(pkg: String, event: AccessibilityEvent) {
        // System UI, keyboards and our own overlay window all raise WINDOW_STATE_CHANGED while
        // the app underneath stays exactly where it is. Recording one as the new foreground app
        // is what caused the flicker: they are all allowlisted, so the very next evaluate() tore
        // the overlay down, re-exposed the blocked app, which re-fired its own event, which
        // re-showed the overlay - forever. They are never a foreground switch; drop them.
        if (pkg in policy.transparent) return

        // A locked Settings page can't be navigated under the overlay, so further events from
        // its package are it settling. Re-reading one could miss the title and drop the block;
        // only a switch to another package (Home, Recents, another app) moves on.
        if (pkg == foregroundPackage && overlayController.isShowing && overlayModel.value?.block is Block.NightPage) {
            return
        }

        // Settings shows most pages in one shared activity, so the same guarded package arriving
        // again can still be a different page: keep track of it, even before a lock starts.
        val guarded = pkg in guard.guardedPackages
        if (pkg == foregroundPackage && !guarded) return

        val now = nightClock.now()
        val night = nightSession(now)
        val readsPage = night != null && guarded

        val className = event.className?.toString()
        val texts = event.text.mapNotNull { it?.toString() }
        val escapePage = readsPage && readEscapePage(pkg, className, texts)
        if (isNoise(incomingBlocked = decide(pkg, now, night, escapePage) != null, nowMs = now.elapsedMs)) return

        foregroundPackage = pkg
        pageClassName = className
        pageTexts = texts
        pageIsEscape = if (readsPage) escapePage else null
        evaluate(now)
    }

    /** Only subscribed during Night lock; see [PAGE_EVENT_TYPES]. */
    private fun onPageContentChanged(pkg: String) {
        if (pkg != foregroundPackage || pkg !in guard.guardedPackages || overlayController.isShowing) return
        // Throttle rather than debounce: a page that never stops animating must still be read.
        if (pageScanJob?.isActive == true) return
        pageScanJob = serviceScope.launch {
            delay(PAGE_RESCAN_MS)
            if (foregroundPackage != pkg || overlayController.isShowing) return@launch
            pageIsEscape = null
            evaluate()
        }
    }

    /**
     * True for an allowed-app event arriving in the first [SHOW_SETTLE_MS] after the overlay went
     * up. The launcher (and other allowlisted system surfaces) keep emitting window-state events
     * for a beat as the app-open animation finishes, well after the blocked app is already
     * foreground. Such an event is *discarded*, not merely deferred - leaving [foregroundPackage]
     * pinned to the blocked app - because a deferred hide would simply fire on the next tick and
     * restart the flicker.
     */
    private fun isNoise(incomingBlocked: Boolean, nowMs: Long): Boolean =
        overlayController.isShowing && nowMs - lastShowAtMs < SHOW_SETTLE_MS && !incomingBlocked

    /** Night lock comes first: ending a focus session can't unblock what Night lock blocks. */
    private fun decide(pkg: String, now: ClockReading, night: NightSession?, escapePage: Boolean): Block? {
        if (night != null) {
            if (escapePage && pkg in guard.guardedPackages) return Block.NightPage(night.until.wallMs)
            if (pkg in night.blockedPackages && pkg !in policy.alwaysAllowed) return Block.NightApp(night.until.wallMs)
        }
        val snap = snapshot
        val focusBlocks = snap.session.isRunning(now.elapsedMs) &&
            snap.switches.strict &&
            pkg !in snap.allowed &&
            pkg !in policy.alwaysAllowed
        return if (focusBlocks) Block.Focus else null
    }

    private fun evaluate(now: ClockReading = nightClock.now()) {
        val night = nightSession(now)
        setPageEventsSubscribed(night != null)
        val pkg = foregroundPackage ?: return

        if (night == null) {
            pageIsEscape = null
        } else if (pageIsEscape == null && pkg in guard.guardedPackages) {
            // The lock started, or the page changed, since the page was last read.
            pageIsEscape = readEscapePage(pkg, pageClassName, pageTexts)
        }

        val block = decide(pkg, now, night, escapePage = pageIsEscape == true)
        if (block == null) {
            // Landing on an allowed app is the only real proof an in-flight exit succeeded.
            exitingUntilMs = 0L
            hideOverlay()
            return
        }

        // Still on a blocked app. While an exit is in flight, hold off re-showing: the overlay
        // would reclaim input focus and swallow the very Back we are trying to inject.
        if (now.elapsedMs < exitingUntilMs) return

        // A lock can start while the phone is locked with a blocked app behind the lock screen.
        // An accessibility overlay would draw over the lock screen itself; ACTION_USER_PRESENT
        // re-checks the moment the phone is unlocked instead.
        if (!overlayController.isShowing && keyguardManager?.isKeyguardLocked == true) return

        showOverlay(pkg, block, night)
    }

    /**
     * Tonight's lock as of [now], or null. When the stored state is behind (a lock is due, a
     * change is due), the advanced state is written back, so a lock is armed on disk - and
     * survives a reboot or a clock change - the moment it is first seen.
     */
    private fun nightSession(now: ClockReading): NightSession? {
        val stored = snapshot.night
        val current = stored.advance(now)
        if (current != stored && nightRefreshJob?.isActive != true) {
            nightRefreshJob = serviceScope.launch { nightRepository.refresh() }
        }
        return current.session
    }

    private fun reanchorNightLock() {
        serviceScope.launch { nightRepository.reanchor() }
    }

    private fun recheckForeground() {
        if (foregroundPackage == null) {
            foregroundPackage = rootInActiveWindow?.packageName?.toString()?.takeUnless { it in policy.transparent }
        }
        // Re-read the page, unless a lock already covers it (see onWindowStateChanged).
        if (!overlayController.isShowing) pageIsEscape = null
        evaluate()
    }

    private fun readEscapePage(pkg: String, className: String?, texts: List<String>): Boolean =
        guard.isEscapePage(
            packageName = pkg,
            className = className,
            eventTexts = texts,
            findRoot = { findPageRoot(pkg) },
            isUninstallProtectionOn = protectionChecker::isUninstallProtectionOn,
        )

    private fun findPageRoot(pkg: String): AccessibilityNodeInfo? {
        rootInActiveWindow?.takeIf { it.packageName?.toString() == pkg }?.let { return it }
        // The active window can be another package's (our overlay, a keyboard): look for the page
        // among the windows on screen, topmost first.
        return windows.asSequence()
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .firstOrNull { it.packageName?.toString() == pkg }
    }

    private fun showOverlay(blockedPackage: String, block: Block, night: NightSession?) {
        // The focus dock suggests allowlisted apps; don't suggest ones Night lock is blocking.
        val dock = if (night == null) dockApps else dockApps.filterNot { it.packageName in night.blockedPackages }
        overlayModel.value = OverlayModel(label = resolveLabel(blockedPackage), block = block, dock = dock)
        if (overlayController.isShowing) return

        val now = SystemClock.elapsedRealtime()
        lastShowAtMs = now
        overlayNowState.longValue = now
        val holdMs = repository.holdToEndMs

        // Same token both overlay contents fill themselves with, read non-composably so the
        // window is opaque from frame one rather than from first composition.
        overlayController.show(
            backgroundColor = OrganicDesignSystem.colors.lockedSurface.argb.toInt(),
            onBack = ::onOverlayBack,
        ) {
            overlayModel.value?.let { model -> OverlayContent(model, holdMs) }
        }

        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(TICK_MS)
                overlayNowState.longValue = SystemClock.elapsedRealtime()
                // The tick exists to notice the session or the night ending underneath a
                // still-blocked app, never to re-decide which app is in front: noise events never
                // move foregroundPackage, so while the overlay is up it is still the blocked app.
                evaluate()
            }
        }
    }

    @Composable
    private fun OverlayContent(model: OverlayModel, holdMs: Int) {
        when (val block = model.block) {
            Block.Focus -> {
                val tick = overlayNowState.longValue
                BlockedOverlayContent(
                    blockedAppLabel = model.label,
                    remainingSeconds = snapshot.session.remainingSeconds(tick),
                    totalSeconds = snapshot.session.totalSeconds,
                    dockApps = model.dock,
                    holdToEndMs = holdMs,
                    onBackToFocus = { returnToApp() },
                    onEndSession = { serviceScope.launch { repository.endSession() } },
                )
            }
            is Block.NightApp -> NightOverlayContent(
                title = "${model.label} is asleep",
                message = "Night lock keeps it closed until morning.",
                initial = model.label.take(1).uppercase(),
                untilText = formatTime(block.untilWallMs),
                actionText = "Close ${model.label}",
                onAction = ::onOverlayBack,
            )
            is Block.NightPage -> NightOverlayContent(
                title = "This page is locked",
                message = "During Night lock, the settings that could switch it off stay closed.",
                initial = null,
                untilText = formatTime(block.untilWallMs),
                actionText = "Leave ${model.label}",
                onAction = ::onOverlayBack,
            )
        }
    }

    private fun formatTime(wallMs: Long): String = DateFormat.getTimeFormat(this).format(Date(wallMs))

    private fun hideOverlay() {
        tickerJob?.cancel()
        tickerJob = null
        if (::overlayController.isInitialized) overlayController.hide()
    }

    /** A locked Settings page goes Home: Back would only step to the page before it. */
    private fun onOverlayBack() {
        leaveBlocked(viaHome = overlayModel.value?.block is Block.NightPage)
    }

    /**
     * One Back press leaves the blocked app for good.
     *
     * Back is injected rather than merely dismissing the overlay, because dismissing alone would
     * drop the user straight back into the blocked app - which is still foreground, and would be
     * re-blocked on its next window event. It is also the reason Back is preferred over Home:
     * a real Back finishes the blocked activity, so its task stops appearing in Recents with a
     * live preview of its content. Home would leave that thumbnail behind.
     *
     * If Back doesn't get us out within [BACK_SETTLE_MS] - a deep back stack, or an app that
     * swallows it - Home is the guaranteed fallback. [viaHome] skips straight to it.
     */
    private fun leaveBlocked(viaHome: Boolean) {
        exitingUntilMs = SystemClock.elapsedRealtime() + EXIT_GRACE_MS
        hideOverlay()
        foregroundPackage = null

        serviceScope.launch {
            delay(FOCUS_RELEASE_MS)
            if (viaHome) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                return@launch
            }
            performGlobalAction(GLOBAL_ACTION_BACK)
            delay(BACK_SETTLE_MS)
            // Still set means no allowed app ever reached the foreground: Back wasn't enough.
            if (exitingUntilMs != 0L) performGlobalAction(GLOBAL_ACTION_HOME)
        }
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

    private fun setPageEventsSubscribed(subscribed: Boolean) {
        if (subscribed == pageEventsSubscribed) return
        val info = serviceInfo ?: return
        info.eventTypes = if (subscribed) BASE_EVENT_TYPES or PAGE_EVENT_TYPES else BASE_EVENT_TYPES
        serviceInfo = info
        pageEventsSubscribed = subscribed
    }

    private fun registerSystemEvents() {
        if (systemEventsRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }
        // All system broadcasts, which still reach a not-exported receiver.
        ContextCompat.registerReceiver(this, systemEvents, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        systemEventsRegistered = true
    }

    private fun unregisterSystemEvents() {
        if (!systemEventsRegistered) return
        systemEventsRegistered = false
        unregisterReceiver(systemEvents)
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
        unregisterSystemEvents()
        serviceScope.cancel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        hideOverlay()
        unregisterSystemEvents()
        serviceScope.cancel()
        super.onDestroy()
    }
}
