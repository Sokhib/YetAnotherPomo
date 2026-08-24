package com.tora.yetanotherpomo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tora.yetanotherpomo.ui.FocusViewModel
import com.tora.yetanotherpomo.ui.screens.allowlist.AllowlistScreen
import com.tora.yetanotherpomo.ui.screens.home.HomeScreen
import com.tora.yetanotherpomo.ui.screens.locked.LockedScreen
import com.tora.yetanotherpomo.ui.screens.settings.SettingsScreen

private object Routes {
    const val HOME = "home"
    const val ALLOWLIST = "allowlist"
    const val SETTINGS = "settings"
}

/**
 * The Home/Allowlist/Settings routes, plus the Locked screen shown exclusively - independent of
 * whatever route is active - whenever a session is running, mirroring how the reference
 * prototype treats Locked as a mutually-exclusive display state rather than a nav destination.
 */
@Composable
fun FocusApp(
    viewModel: FocusViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                onMinutesChange = viewModel::setMinutes,
                onBegin = viewModel::beginSession,
                onOpenAllowlist = { navController.navigate(Routes.ALLOWLIST) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
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
                onToggleSwitch = viewModel::toggleSwitch,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
