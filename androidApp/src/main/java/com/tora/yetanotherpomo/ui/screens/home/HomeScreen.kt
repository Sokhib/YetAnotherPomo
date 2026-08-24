package com.tora.yetanotherpomo.ui.screens.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.FocusUiState
import com.tora.yetanotherpomo.ui.components.AppDockPreview
import com.tora.yetanotherpomo.ui.components.CircularDial
import com.tora.yetanotherpomo.ui.components.DockEntry
import com.tora.yetanotherpomo.ui.components.PillButton
import com.tora.yetanotherpomo.ui.components.PillButtonVariant
import com.tora.yetanotherpomo.ui.theme.Organic

private val PRESETS = listOf(15, 25, 50)

@Composable
fun HomeScreen(
    uiState: FocusUiState,
    onMinutesChange: (Int) -> Unit,
    onBegin: () -> Unit,
    onOpenAllowlist: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .padding(horizontal = 24.dp, vertical = 22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Focus Lock", style = type.heading3, color = colors.text)
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Settings", tint = colors.neutral.c700)
            }
        }

        if (!uiState.isAccessibilityGranted) {
            AccessibilityBanner(
                onGrant = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                },
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularDial(
                minutes = uiState.minutes,
                onMinutesChange = onMinutesChange,
                maxMinutes = uiState.dialMaxMinutes,
            )
            Row(
                modifier = Modifier.padding(top = 30.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                PRESETS.forEach { preset ->
                    val selected = preset == uiState.minutes
                    PillButton(
                        text = "$preset min",
                        onClick = { onMinutesChange(preset) },
                        variant = if (selected) PillButtonVariant.Primary else PillButtonVariant.Secondary,
                        containerColor = if (selected) colors.accentRamp.c200 else androidx.compose.ui.graphics.Color.Transparent,
                        contentColor = if (selected) colors.accentRamp.c800 else colors.neutral.c700,
                        borderColor = if (selected) colors.accentRamp.c300 else colors.divider,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            val dockEntries = uiState.installedApps
                .filter { it.packageName in uiState.allowedPackages }
                .mapIndexed { index, app -> DockEntry(app.packageName, app.label, index) }
            AppDockPreview(
                apps = dockEntries,
                totalAllowed = dockEntries.size,
                onClick = onOpenAllowlist,
            )
            PillButton(
                text = "Begin ${uiState.minutes} minutes",
                onClick = onBegin,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun AccessibilityBanner(onGrant: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Organic.colors
    val type = Organic.type
    val radius = Organic.radius

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.accentRamp.c200)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Focus Lock needs Accessibility access to block apps during a session.",
            style = type.bodySmall,
            color = colors.accentRamp.c800,
        )
        PillButton(
            text = "Grant access",
            onClick = onGrant,
            variant = PillButtonVariant.Secondary,
            containerColor = colors.accentRamp.c800,
            contentColor = colors.bg,
        )
    }
}
