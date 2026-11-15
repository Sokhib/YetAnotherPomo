package com.tora.yetanotherpomo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tora.yetanotherpomo.ui.FocusViewModel
import com.tora.yetanotherpomo.ui.NightLockUiState
import com.tora.yetanotherpomo.ui.NightLockViewModel
import com.tora.yetanotherpomo.ui.screens.allowlist.AllowlistScreen
import com.tora.yetanotherpomo.ui.screens.home.HomeScreen
import com.tora.yetanotherpomo.ui.screens.locked.LockedScreen
import com.tora.yetanotherpomo.ui.screens.night.NightBlocklistScreen
import com.tora.yetanotherpomo.ui.screens.night.NightLockScreen
import com.tora.yetanotherpomo.ui.screens.night.formatWallTime
import com.tora.yetanotherpomo.ui.screens.settings.SettingsScreen

private object Routes {
    const val HOME = "home"
    const val ALLOWLIST = "allowlist"
    const val SETTINGS = "settings"
    const val NIGHT_LOCK = "night_lock"
    const val NIGHT_BLOCKLIST = "night_lock/blocklist"
}

/**
 * The Home/Allowlist/Settings/Night lock routes, plus the Locked screen shown exclusively -
 * independent of whatever route is active - whenever a session is running, mirroring how the
 * reference prototype treats Locked as a mutually-exclusive display state rather than a nav
 * destination.
 */
@Composable
fun FocusApp(
    viewModel: FocusViewModel,
    nightLockViewModel: NightLockViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nightState by nightLockViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (uiState.isRunning) {
        LockedScreen(
            uiState = uiState,
            onEndSession = viewModel::endSessionEarly,
            modifier = modifier,
        )
        return
    }

    NavHost(navController = navController, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState = uiState,
                nightLockUntil = nightState.activeUntilWallMs?.let { formatWallTime(context, it) },
                onMinutesChange = viewModel::setMinutes,
                onBegin = viewModel::beginSession,
                onOpenAllowlist = { navController.navigate(Routes.ALLOWLIST) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenNightLock = { navController.navigate(Routes.NIGHT_LOCK) },
            )
        }
        composable(Routes.ALLOWLIST) {
            AllowlistScreen(
                installedApps = uiState.installedApps,
                allowedPackages = uiState.allowedPackages,
                onToggleApp = viewModel::toggleAppAllowed,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                minutes = uiState.minutes,
                allowedCount = uiState.allowedPackages.size,
                switches = uiState.switches,
                nightLockSummary = nightLockSummary(nightState) { formatWallTime(context, it) },
                onToggleSwitch = viewModel::toggleSwitch,
                onOpenNightLock = { navController.navigate(Routes.NIGHT_LOCK) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.NIGHT_LOCK) {
            // Counted against installed apps: the default list also covers apps not installed yet.
            val blockedCount = remember(uiState.installedApps, nightState.desired.blockedPackages, nightState.alwaysReachablePackages) {
                uiState.installedApps.count {
                    it.packageName in nightState.desired.blockedPackages &&
                        it.packageName !in nightState.alwaysReachablePackages
                }
            }
            NightLockScreen(
                uiState = nightState,
                blockedAppCount = blockedCount,
                onEnabledChange = nightLockViewModel::setEnabled,
                onStartMinuteChange = nightLockViewModel::setStartMinute,
                onEndMinuteChange = nightLockViewModel::setEndMinute,
                onOpenBlockedApps = { navController.navigate(Routes.NIGHT_BLOCKLIST) },
                onDiscardPendingChange = nightLockViewModel::discardPendingChange,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.NIGHT_BLOCKLIST) {
            NightBlocklistScreen(
                installedApps = uiState.installedApps,
                blockedPackages = nightState.desired.blockedPackages,
                alwaysReachablePackages = nightState.alwaysReachablePackages,
                hasPendingChange = nightState.pendingAppliesAtWallMs != null,
                onToggleApp = nightLockViewModel::toggleBlocked,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun nightLockSummary(state: NightLockUiState, formatTime: (Long) -> String): String = when {
    state.activeUntilWallMs != null -> "On until ${formatTime(state.activeUntilWallMs)}"
    state.nextStartWallMs != null -> "From ${formatTime(state.nextStartWallMs)}"
    else -> "Off"
}
