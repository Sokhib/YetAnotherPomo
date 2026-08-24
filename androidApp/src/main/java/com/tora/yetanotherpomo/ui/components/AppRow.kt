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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tora.yetanotherpomo.ui.theme.Organic

/** An Allowlist row: avatar, name + note, trailing switch. Matches the doc's `sc-for apps` row. */
@Composable
fun AppRow(
    label: String,
    note: String,
    packageName: String,
    toneBg: Color,
    toneFg: Color,
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
            .padding(vertical = 11.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            packageName = packageName,
            label = label,
            size = 42.dp,
            toneBg = toneBg,
            toneFg = toneFg,
        )
        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f),
        ) {
            Text(text = label, style = type.label, color = colors.text)
            Text(text = note, style = type.labelSmall.copy(letterSpacing = 0.sp), color = colors.neutral.c600)
        }
        OrganicSwitch(checked = checked, onCheckedChange = { onToggle() })
    }
}
