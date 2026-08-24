package com.tora.yetanotherpomo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.Organic

/** A labeled switch row for Settings ("Full-screen lock", "Long-press to break out", ...). */
@Composable
fun SwitchRow(
    label: String,
    note: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Organic.size.minTouchTarget)
            .clickable(onClick = onToggle)
            .padding(vertical = 13.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = type.label, color = colors.text)
            Text(text = note, style = type.bodySmall, color = colors.neutral.c600)
        }
        OrganicSwitch(checked = checked, onCheckedChange = { onToggle() })
    }
}
