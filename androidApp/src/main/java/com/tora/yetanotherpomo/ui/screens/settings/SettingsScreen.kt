package com.tora.yetanotherpomo.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.domain.model.FocusSwitches
import com.tora.yetanotherpomo.ui.components.SwitchRow
import com.tora.yetanotherpomo.ui.theme.Organic

private data class SwitchMeta(val name: String, val note: String, val checked: Boolean, val toggle: () -> Unit)

@Composable
fun SettingsScreen(
    minutes: Int,
    allowedCount: Int,
    switches: FocusSwitches,
    onToggleSwitch: (update: (FocusSwitches) -> FocusSwitches) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type

    val switchRows = listOf(
        SwitchMeta(
            name = "Full-screen lock",
            note = "Holds the screen so blocked apps cannot be reached",
            checked = switches.strict,
            toggle = { onToggleSwitch { it.copy(strict = !it.strict) } },
        ),
        SwitchMeta(
            name = "Long-press to break out",
            note = "Hold the button, then confirm",
            checked = switches.holdToBreakOut,
            toggle = { onToggleSwitch { it.copy(holdToBreakOut = !it.holdToBreakOut) } },
        ),
        SwitchMeta(
            name = "Chime at finish",
            note = "A soft tone when the session ends",
            checked = switches.chime,
            toggle = { onToggleSwitch { it.copy(chime = !it.chime) } },
        ),
        SwitchMeta(
            name = "Keep the clock visible",
            note = "Shows the countdown while locked",
            checked = switches.keepClockVisible,
            toggle = { onToggleSwitch { it.copy(keepClockVisible = !it.keepClockVisible) } },
        ),
    )

    Column(modifier = modifier.fillMaxSize().background(colors.bg)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
        }
        Text(
            text = "Settings",
            style = type.heading2,
            color = colors.text,
            modifier = Modifier.padding(horizontal = Organic.size.screenGutter, vertical = 6.dp),
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = Organic.size.listGutter)) {
            item {
                Text(
                    text = "DEFAULTS",
                    style = type.labelSmall,
                    color = colors.neutral.c600,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp, top = 6.dp),
                )
            }
            item { SettingsRow(label = "Session length", value = "$minutes min") }
            item { SettingsRow(label = "Short break", value = "5 min") }
            item { SettingsRow(label = "Allowed apps", value = "$allowedCount apps") }

            item {
                Text(
                    text = "THE LOCK",
                    style = type.labelSmall,
                    color = colors.neutral.c600,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp, top = 22.dp),
                )
            }
            items(switchRows) { row ->
                SwitchRow(label = row.name, note = row.note, checked = row.checked, onToggle = row.toggle)
            }

            item {
                Text(
                    text = "Focus Lock holds the screen with an Accessibility service. Calls from your allowlist always come through.",
                    style = type.bodySmall,
                    color = colors.neutral.c600,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    val colors = Organic.colors
    val type = Organic.type
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Organic.size.minTouchTarget)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = type.label, color = colors.text)
        Text(text = value, style = type.body, color = colors.accentRamp.c700)
    }
}
