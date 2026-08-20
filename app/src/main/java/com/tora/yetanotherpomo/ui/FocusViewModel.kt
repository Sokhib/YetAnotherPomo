package com.tora.yetanotherpomo.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tora.yetanotherpomo.di.AppContainer
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.domain.repository.FocusRepository
import com.tora.yetanotherpomo.domain.repository.InstalledAppsRepository
import com.tora.yetanotherpomo.domain.model.InstalledApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The single shared ViewModel for all four in-app screens (Home/Allowlist/Locked/Settings).
 * Combines the repository's three flows with a 1s ticker into one [uiState] - the ticker derives
 * remaining time from [SystemClock.elapsedRealtime] each tick rather than naively decrementing,
 * so backgrounding never causes drift.
 */
class FocusViewModel(
    private val repository: FocusRepository,
    private val installedAppsRepository: InstalledAppsRepository,
    private val accessibilityStatusChecker: AccessibilityStatusChecker,
) : ViewModel() {

    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val accessibilityGranted = MutableStateFlow(accessibilityStatusChecker.isServiceEnabled())

    private val ticker = flow {
        while (true) {
            emit(SystemClock.elapsedRealtime())
            delay(1000)
        }
    }

    val uiState: StateFlow<FocusUiState> = combine(
        combine(repository.sessionFlow, repository.allowedFlow, repository.switchesFlow) { session, allowed, switches ->
            Triple(session, allowed, switches)
        },
        combine(installedApps, accessibilityGranted) { apps, granted -> apps to granted },
        ticker,
    ) { (session, allowed, switches), (apps, granted), now ->
        FocusUiState(
            minutes = session.minutes,
            remainingSeconds = session.remainingSeconds(now),
            totalSeconds = session.totalSeconds,
            isRunning = session.isRunning(now),
            progressFraction = session.progressFraction(now),
            allowedPackages = allowed,
            installedApps = apps,
            switches = switches,
            isAccessibilityGranted = granted,
            dialMaxMinutes = repository.dialMaxMinutes,
            holdToEndMs = repository.holdToEndMs,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusUiState.Initial)

    init {
        viewModelScope.launch { installedApps.value = installedAppsRepository.getLaunchableApps() }
    }

    fun setMinutes(minutes: Int) {
        viewModelScope.launch { repository.setMinutes(minutes.coerceIn(5, repository.dialMaxMinutes)) }
    }

    fun beginSession() {
        viewModelScope.launch { repository.startSession(uiState.value.minutes) }
    }

    fun endSessionEarly() {
        viewModelScope.launch { repository.endSession() }
    }

    fun toggleAppAllowed(packageName: String) {
        val nowAllowed = packageName !in uiState.value.allowedPackages
        viewModelScope.launch { repository.setAppAllowed(packageName, nowAllowed) }
    }

    fun toggleSwitch(update: (FocusSwitches) -> FocusSwitches) {
        viewModelScope.launch { repository.setSwitch(update) }
    }

    fun refreshAccessibilityStatus() {
        accessibilityGranted.value = accessibilityStatusChecker.isServiceEnabled()
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer {
                FocusViewModel(
                    repository = container.focusRepository,
                    installedAppsRepository = container.installedAppsRepository,
                    accessibilityStatusChecker = container.accessibilityStatusChecker,
                )
            }
        }
    }
}
