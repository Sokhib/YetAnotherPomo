package com.tora.yetanotherpomo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tora.yetanotherpomo.di.AppContainer
import com.tora.yetanotherpomo.domain.model.NightLockConfig
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import com.tora.yetanotherpomo.domain.repository.NightClock
import com.tora.yetanotherpomo.domain.repository.NightLockRepository
import com.tora.yetanotherpomo.domain.repository.NightProtectionChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * A lock starts and ends on the wall clock without anything being written, so the status is
 * re-derived on this tick. Coarse on purpose: the screen shows minutes, not seconds.
 */
private const val STATUS_TICK_MS = 5_000L

data class NightLockUiState(
    /** What the editor shows: the person's latest request, waiting or not. */
    val desired: NightLockConfig,
    /** When tonight's running lock ends; null while no lock is running. */
    val activeUntilWallMs: Long?,
    /** When the next lock starts; null while one is running or Night lock is off. */
    val nextStartWallMs: Long?,
    /** When a waiting, looser change takes over; null when nothing is waiting. */
    val pendingAppliesAtWallMs: Long?,
    /** Never blocked, so left out of the app picker. */
    val alwaysReachablePackages: Set<String>,
    val isAccessibilityOn: Boolean,
    val isUninstallProtectionOn: Boolean,
    val isBatteryUnrestricted: Boolean,
) {
    val isLocking: Boolean get() = activeUntilWallMs != null

    companion object {
        val Initial = NightLockUiState(
            desired = NightLockConfig.Default,
            activeUntilWallMs = null,
            nextStartWallMs = null,
            pendingAppliesAtWallMs = null,
            alwaysReachablePackages = emptySet(),
            isAccessibilityOn = false,
            isUninstallProtectionOn = false,
            isBatteryUnrestricted = false,
        )
    }
}

/**
 * Night lock's settings. Edits are handed to [NightLockRepository.edit] as transforms of the
 * stored request, so the stricter-now, looser-later rule is applied to the latest state inside
 * one store transaction, never to a possibly stale copy held here.
 */
class NightLockViewModel(
    private val repository: NightLockRepository,
    private val clock: NightClock,
    private val protectionChecker: NightProtectionChecker,
    private val accessibilityStatusChecker: AccessibilityStatusChecker,
    private val installedAppsRepository: InstalledAppsRepository,
) : ViewModel() {

    private data class Protection(val accessibility: Boolean, val uninstall: Boolean, val battery: Boolean)

    private val protection = MutableStateFlow(readProtection())
    private val alwaysReachable = MutableStateFlow<Set<String>>(emptySet())

    private val ticker = flow {
        while (true) {
            emit(Unit)
            delay(STATUS_TICK_MS)
        }
    }

    val uiState: StateFlow<NightLockUiState> = combine(
        repository.stateFlow,
        protection,
        alwaysReachable,
        ticker,
    ) { stored, protection, reachable, _ ->
        val now = clock.now()
        val state = stored.advance(now)
        NightLockUiState(
            desired = state.desired,
            activeUntilWallMs = state.session?.until?.wallMs,
            nextStartWallMs = if (state.session == null) state.nextStartWallMs(now) else null,
            pendingAppliesAtWallMs = state.pending?.appliesAt?.wallMs,
            alwaysReachablePackages = reachable,
            isAccessibilityOn = protection.accessibility,
            isUninstallProtectionOn = protection.uninstall,
            isBatteryUnrestricted = protection.battery,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NightLockUiState.Initial)

    init {
        viewModelScope.launch { alwaysReachable.value = installedAppsRepository.getAlwaysReachablePackages() }
    }

    fun setEnabled(enabled: Boolean) = edit { it.copy(enabled = enabled) }

    fun setStartMinute(minute: Int) = edit { it.copy(window = it.window.copy(startMinute = minute)) }

    fun setEndMinute(minute: Int) = edit { it.copy(window = it.window.copy(endMinute = minute)) }

    fun toggleBlocked(packageName: String) = edit { config ->
        val blocked = config.blockedPackages
        config.copy(blockedPackages = if (packageName in blocked) blocked - packageName else blocked + packageName)
    }

    fun discardPendingChange() {
        viewModelScope.launch { repository.discardPendingChange() }
    }

    /** Re-read on every resume: each of these is switched on in a system screen and comes back here. */
    fun refreshProtection() {
        protection.value = readProtection()
    }

    private fun edit(transform: (NightLockConfig) -> NightLockConfig) {
        viewModelScope.launch { repository.edit(transform) }
    }

    private fun readProtection() = Protection(
        accessibility = accessibilityStatusChecker.isServiceEnabled(),
        uninstall = protectionChecker.isUninstallProtectionOn(),
        battery = protectionChecker.isBatteryUnrestricted(),
    )

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                NightLockViewModel(
                    repository = container.nightLockRepository,
                    clock = container.nightClock,
                    protectionChecker = container.nightProtectionChecker,
                    accessibilityStatusChecker = container.accessibilityStatusChecker,
                    installedAppsRepository = container.installedAppsRepository,
                )
            }
        }
    }
}
