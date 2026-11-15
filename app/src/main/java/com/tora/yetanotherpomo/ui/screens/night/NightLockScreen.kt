package com.tora.yetanotherpomo.ui.screens.night

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.tora.yetanotherpomo.receiver.NightLockAdminReceiver
import com.tora.yetanotherpomo.ui.NightLockUiState
import com.tora.yetanotherpomo.ui.components.PillButton
import com.tora.yetanotherpomo.ui.components.PillButtonVariant
import com.tora.yetanotherpomo.ui.components.SwitchRow
import com.tora.yetanotherpomo.ui.theme.Organic

private enum class TimeField { Start, End }

@Composable
fun NightLockScreen(
    uiState: NightLockUiState,
    blockedAppCount: Int,
    onEnabledChange: (Boolean) -> Unit,
    onStartMinuteChange: (Int) -> Unit,
    onEndMinuteChange: (Int) -> Unit,
    onOpenBlockedApps: () -> Unit,
    onDiscardPendingChange: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type
    val context = LocalContext.current
    val desired = uiState.desired
    var editing by rememberSaveable { mutableStateOf<TimeField?>(null) }

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
        }
        Column(modifier = Modifier.padding(horizontal = Organic.size.screenGutter, vertical = 6.dp)) {
            Text(text = "Night lock", style = type.heading2, color = colors.text)
            Text(
                text = "Every night, the apps you pick stay closed. Once it starts, it can't be switched off until morning.",
                style = type.bodySmall,
                color = colors.neutral.c700,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = Organic.size.listGutter)) {
            item { StatusCard(uiState) }

            uiState.pendingAppliesAtWallMs?.let { appliesAt ->
                item { PendingCard(appliesAtText = formatWallDayAndTime(context, appliesAt), onUndo = onDiscardPendingChange) }
            }

            item { SectionLabel("SCHEDULE") }
            item {
                SwitchRow(
                    label = "Night lock",
                    note = "Blocks the apps you pick, every night",
                    checked = desired.enabled,
                    onToggle = { onEnabledChange(!desired.enabled) },
                )
            }
            item {
                NightRow(
                    label = "Starts",
                    value = formatMinuteOfDay(context, desired.window.startMinute),
                    onClick = { editing = TimeField.Start },
                )
            }
            item {
                NightRow(
                    label = "Ends",
                    value = formatMinuteOfDay(context, desired.window.endMinute),
                    onClick = { editing = TimeField.End },
                )
            }
            item {
                NightRow(
                    label = "Blocked apps",
                    value = if (blockedAppCount == 1) "1 app" else "$blockedAppCount apps",
                    onClick = onOpenBlockedApps,
                )
            }

            item { SectionLabel("KEEP IT ON") }
            item {
                NightRow(
                    label = "Accessibility",
                    note = "Without it, Night lock can't block anything",
                    value = if (uiState.isAccessibilityOn) "On" else "Turn on",
                    onClick = if (uiState.isAccessibilityOn) null else { { context.openAccessibilitySettings() } },
                )
            }
            item {
                NightRow(
                    label = "Uninstall protection",
                    note = "Focus Lock can't be uninstalled while this is on",
                    value = if (uiState.isUninstallProtectionOn) "On" else "Turn on",
                    onClick = if (uiState.isUninstallProtectionOn) null else { { context.requestUninstallProtection() } },
                )
            }
            item {
                NightRow(
                    label = "Battery",
                    note = "Stops the phone from closing Focus Lock overnight",
                    value = if (uiState.isBatteryUnrestricted) "Unrestricted" else "Allow",
                    onClick = if (uiState.isBatteryUnrestricted) null else { { context.requestBatteryExemption() } },
                )
            }

            item {
                Text(
                    text = "Night lock stops the impulse, not a plan: safe mode, a computer with USB debugging, " +
                        "or a factory reset can still get around it.",
                    style = type.bodySmall,
                    color = colors.neutral.c600,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                )
            }
        }
    }

    when (editing) {
        TimeField.Start -> NightTimeDialog(
            title = "Night lock starts",
            initialMinute = desired.window.startMinute,
            otherBoundMinute = desired.window.endMinute,
            onConfirm = { minute ->
                editing = null
                onStartMinuteChange(minute)
            },
            onDismiss = { editing = null },
        )
        TimeField.End -> NightTimeDialog(
            title = "Night lock ends",
            initialMinute = desired.window.endMinute,
            otherBoundMinute = desired.window.startMinute,
            onConfirm = { minute ->
                editing = null
                onEndMinuteChange(minute)
            },
            onDismiss = { editing = null },
        )
        null -> Unit
    }
}

@Composable
private fun StatusCard(uiState: NightLockUiState) {
    val colors = Organic.colors
    val type = Organic.type
    val context = LocalContext.current

    val (headline, body) = when {
        uiState.activeUntilWallMs != null ->
            "On until ${formatWallTime(context, uiState.activeUntilWallMs)}" to
                "Your blocked apps, and the settings that could switch Night lock off, stay closed."
        uiState.nextStartWallMs != null ->
            "Starts at ${formatWallTime(context, uiState.nextStartWallMs)}" to
                "Once it starts, nothing here can stop it before it ends."
        else -> "Off" to "Pick your apps and a bedtime, then switch it on."
    }

    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(Organic.radius.lg))
            .background(if (uiState.isLocking) colors.lockedSurface else colors.surface)
            .padding(vertical = 16.dp, horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val onCard = if (uiState.isLocking) colors.lockedOnSurface else colors.text
        Text(text = headline, style = type.heading3, color = onCard)
        Text(text = body, style = type.bodySmall, color = onCard.copy(alpha = Organic.opacity.secondary))
        val scheduled = uiState.isLocking || uiState.nextStartWallMs != null
        if (scheduled && !uiState.isAccessibilityOn) {
            Text(
                text = "Accessibility is off, so nothing is being blocked.",
                style = type.label,
                color = colors.accentRamp.c600,
            )
        }
    }
}

@Composable
private fun PendingCard(appliesAtText: String, onUndo: () -> Unit) {
    val colors = Organic.colors
    Column(
        modifier = Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(Organic.radius.md))
            .background(colors.accentRamp.c200)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Your changes loosen the lock, so they wait until tonight's lock is over. They start $appliesAtText.",
            style = Organic.type.bodySmall,
            color = colors.accentRamp.c800,
        )
        PillButton(
            text = "Undo changes",
            onClick = onUndo,
            variant = PillButtonVariant.Secondary,
            containerColor = colors.accentRamp.c800,
            contentColor = colors.bg,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = Organic.type.labelSmall,
        color = Organic.colors.neutral.c600,
        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp, top = 22.dp),
    )
}

/** A Settings-style row: label (and note) on the left, value on the right; tappable when [onClick] is set. */
@Composable
private fun NightRow(label: String, value: String, onClick: (() -> Unit)?, note: String? = null) {
    val colors = Organic.colors
    val type = Organic.type
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Organic.size.minTouchTarget)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 13.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = type.label, color = colors.text)
            if (note != null) Text(text = note, style = type.bodySmall, color = colors.neutral.c600)
        }
        Text(
            text = value,
            style = type.body,
            color = if (onClick != null) colors.accentRamp.c700 else colors.neutral.c600,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NightTimeDialog(
    title: String,
    initialMinute: Int,
    otherBoundMinute: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = Organic.colors
    val context = LocalContext.current
    val state = rememberTimePickerState(
        initialHour = initialMinute / 60,
        initialMinute = initialMinute % 60,
        is24Hour = DateFormat.is24HourFormat(context),
    )
    val picked = state.hour * 60 + state.minute
    // Equal start and end would be an empty window; refuse rather than guess "all day".
    val clashes = picked == otherBoundMinute

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.bg,
        title = { Text(text = title, style = Organic.type.heading3, color = colors.text) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = state)
                if (clashes) {
                    Text(
                        text = "Start and end can't be the same time.",
                        style = Organic.type.bodySmall,
                        color = colors.accentRamp.c700,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(picked) }, enabled = !clashes) { Text("Set") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun Context.openAccessibilitySettings() {
    startActivitySafely(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
}

private fun Context.requestUninstallProtection() {
    startActivitySafely(
        Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(this, NightLockAdminReceiver::class.java))
            .putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Focus Lock can't be uninstalled while this is on. Switch it off here first if you ever want to remove the app.",
            ),
    )
}

/** Asks directly; the general battery list is the fallback where the direct ask isn't offered. */
@SuppressLint("BatteryLife") // Sideloaded self-control app: staying alive is the feature.
private fun Context.requestBatteryExemption() {
    val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri())
    if (!startActivitySafely(direct)) startActivitySafely(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
}

private fun Context.startActivitySafely(intent: Intent): Boolean = try {
    startActivity(intent)
    true
} catch (e: ActivityNotFoundException) {
    false
}
