package com.tora.yetanotherpomo.ui.screens.locked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.FocusUiState
import com.tora.yetanotherpomo.ui.components.AppDockRow
import com.tora.yetanotherpomo.ui.components.ConfirmEndSheet
import com.tora.yetanotherpomo.ui.components.DockEntry
import com.tora.yetanotherpomo.ui.components.HoldToConfirmButton
import com.tora.yetanotherpomo.ui.theme.Organic

/** Screen 3: the in-app countdown shown while a session is running. */
@Composable
fun LockedScreen(
    uiState: FocusUiState,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type
    val opacity = Organic.opacity
    val sizes = Organic.size
    var confirmVisible by remember { mutableStateOf(false) }

    val mm = (uiState.remainingSeconds / 60).toString().padStart(2, '0')
    val ss = (uiState.remainingSeconds % 60).toString().padStart(2, '0')

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.lockedSurface)
            .padding(horizontal = sizes.screenGutter, vertical = 30.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = colors.lockedOnSurface.copy(alpha = opacity.secondary),
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = "SCREEN LOCKED",
                    style = type.labelSmall,
                    color = colors.lockedOnSurface.copy(alpha = opacity.secondary),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "$mm:$ss", style = type.clockDigits, color = colors.lockedOnSurface)
                Box(
                    modifier = Modifier
                        .padding(top = 26.dp)
                        .width(190.dp)
                        .height(3.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(colors.lockedOnSurface.copy(alpha = opacity.divider)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(uiState.progressFraction)
                            .background(colors.accentRamp.c400),
                    )
                }
                Text(
                    text = "of a ${uiState.minutes} minute session",
                    style = type.bodySmall,
                    color = colors.lockedOnSurface.copy(alpha = opacity.muted),
                    modifier = Modifier.padding(top = 13.dp),
                )
            }

            if (uiState.allowedPackages.isNotEmpty()) {
                val dockApps = uiState.installedApps
                    .filter { it.packageName in uiState.allowedPackages }
                    .mapIndexed { index, app -> DockEntry(app.packageName, app.label, index) }
                AppDockRow(
                    label = "REACHABLE",
                    apps = dockApps,
                    avatarSize = sizes.minTouchTarget,
                    modifier = Modifier.padding(bottom = 30.dp).fillMaxWidth(),
                )
            }

            if (uiState.switches.holdToBreakOut) {
                HoldToConfirmButton(
                    text = "Hold to end early",
                    holdDurationMs = uiState.holdToEndMs,
                    onHoldComplete = { confirmVisible = true },
                )
            }
        }

        if (confirmVisible) {
            ConfirmEndSheet(
                bodyText = "${uiState.remainingSeconds / 60} minutes left. Every blocked app unlocks right away.",
                onKeepFocusing = { confirmVisible = false },
                onEndSession = {
                    confirmVisible = false
                    onEndSession()
                },
            )
        }
    }
}
