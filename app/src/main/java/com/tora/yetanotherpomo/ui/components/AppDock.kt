package com.tora.yetanotherpomo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.Organic

/** An app allowed to stay reachable during a session - the UI-layer shape the dock rows render. */
data class DockEntry(val packageName: String, val label: String, val toneIndex: Int)

/** Home's "Still allowed" surface row: up to 4 avatars + a rest count, tap to edit the allowlist. */
@Composable
fun AppDockPreview(
    apps: List<DockEntry>,
    totalAllowed: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type
    val radius = Organic.radius

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.lg))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(text = "STILL ALLOWED", style = type.labelSmall, color = colors.neutral.c700)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                apps.take(4).forEach { app ->
                    val (bg, fg) = appAvatarTone(app.toneIndex)
                    AppAvatar(packageName = app.packageName, label = app.label, size = 32.dp, toneBg = bg, toneFg = fg)
                }
                val restLabel = when {
                    totalAllowed > 4 -> "+${totalAllowed - 4}"
                    totalAllowed == 0 -> "nothing yet"
                    else -> ""
                }
                if (restLabel.isNotEmpty()) {
                    Text(text = restLabel, style = type.bodySmall, color = colors.neutral.c700)
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.neutral.c600,
        )
    }
}

/** The centered dock shown on the dark Locked/Blocked screens ("reachable" / "open instead"). */
@Composable
fun AppDockRow(
    label: String,
    apps: List<DockEntry>,
    avatarSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val onSurface = Organic.colors.lockedOnSurface
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = label, style = Organic.type.labelSmall, color = onSurface.copy(alpha = Organic.opacity.ghost))
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            apps.take(4).forEach { app ->
                DarkDockAvatar(app = app, size = avatarSize, tint = onSurface)
            }
        }
    }
}

@Composable
private fun DarkDockAvatar(app: DockEntry, size: androidx.compose.ui.unit.Dp, tint: Color) {
    val container = tint.copy(alpha = Organic.opacity.fill)
    val content = tint.copy(alpha = Organic.opacity.strong)
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.background(container, androidx.compose.foundation.shape.CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        AppAvatar(
            packageName = app.packageName,
            label = app.label,
            size = size,
            toneBg = Color.Transparent,
            toneFg = content,
        )
    }
}
