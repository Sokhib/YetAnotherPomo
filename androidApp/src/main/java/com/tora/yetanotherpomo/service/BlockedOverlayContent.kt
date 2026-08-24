package com.tora.yetanotherpomo.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.tora.yetanotherpomo.ui.components.AppDockRow
import com.tora.yetanotherpomo.ui.components.ConfirmEndSheet
import com.tora.yetanotherpomo.ui.components.DockEntry
import com.tora.yetanotherpomo.ui.components.HoldToConfirmButton
import com.tora.yetanotherpomo.ui.components.PillButton
import com.tora.yetanotherpomo.ui.theme.Organic
import com.tora.yetanotherpomo.ui.theme.OrganicTheme

/**
 * Screen 4, "Blocked app opened" - the AccessibilityService's overlay content when the user
 * opens an app that isn't on the allowlist during an active session. A standalone composition
 * root (outside the Activity's tree), so it wraps itself in [OrganicTheme].
 */
@Composable
fun BlockedOverlayContent(
    blockedAppLabel: String,
    remainingSeconds: Int,
    totalSeconds: Int,
    dockApps: List<DockEntry>,
    holdToEndMs: Int,
    onBackToFocus: () -> Unit,
    onEndSession: () -> Unit,
) {
    OrganicTheme {
        val colors = Organic.colors
        val type = Organic.type
        val opacity = Organic.opacity
        val sizes = Organic.size
        var confirmVisible by remember { mutableStateOf(false) }
        val mm = (remainingSeconds / 60).toString().padStart(2, '0')
        val ss = (remainingSeconds % 60).toString().padStart(2, '0')

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.lockedSurface)
                .padding(horizontal = sizes.screenGutter, vertical = 30.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(colors.lockedOnSurface.copy(alpha = opacity.faint)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = blockedAppLabel.take(1).uppercase(),
                            style = type.heading3,
                            color = colors.lockedOnSurface.copy(alpha = opacity.muted),
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(top = 20.dp),
                    ) {
                        Text(text = "$blockedAppLabel is paused", style = type.heading2, color = colors.lockedOnSurface)
                        Text(
                            text = "You left it off the allowlist for this session.",
                            style = type.bodySmall,
                            color = colors.lockedOnSurface.copy(alpha = opacity.secondary),
                        )
                    }
                    Box(modifier = Modifier.padding(top = 6.dp)) {
                        androidx.compose.foundation.layout.Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(text = "$mm:$ss", style = type.heading1, color = colors.accentRamp.c400)
                            Text(
                                text = "left",
                                style = type.bodySmall,
                                color = colors.lockedOnSurface.copy(alpha = opacity.muted),
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                        }
                    }
                }

                if (dockApps.isNotEmpty()) {
                    AppDockRow(
                        label = "OPEN INSTEAD",
                        apps = dockApps,
                        avatarSize = 46.dp,
                        modifier = Modifier.padding(bottom = 26.dp).fillMaxWidth(),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillButton(
                        text = "Back to focus",
                        onClick = onBackToFocus,
                        fullWidth = true,
                        containerColor = colors.accent,
                        contentColor = colors.lockedSurface,
                    )
                    HoldToConfirmButton(
                        text = "Hold to end the session",
                        holdDurationMs = holdToEndMs,
                        onHoldComplete = { confirmVisible = true },
                        minHeight = 48.dp,
                        borderColor = androidx.compose.ui.graphics.Color.Transparent,
                        fillColor = colors.lockedOnSurface.copy(alpha = opacity.fill),
                        contentColor = colors.lockedOnSurface.copy(alpha = opacity.muted),
                    )
                }
            }

            if (confirmVisible) {
                ConfirmEndSheet(
                    bodyText = "${remainingSeconds / 60} minutes left. Every blocked app unlocks right away.",
                    onKeepFocusing = { confirmVisible = false },
                    onEndSession = {
                        confirmVisible = false
                        onEndSession()
                    },
                )
            }
        }
    }
}
