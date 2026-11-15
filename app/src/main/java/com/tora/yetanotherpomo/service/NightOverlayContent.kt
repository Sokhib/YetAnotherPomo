package com.tora.yetanotherpomo.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.components.PillButton
import com.tora.yetanotherpomo.ui.theme.Organic
import com.tora.yetanotherpomo.ui.theme.OrganicTheme

/**
 * The block screen while Night lock runs. Unlike [BlockedOverlayContent] it has no way to end the
 * lock - that is the point - only a way out of the blocked app or page.
 *
 * @param initial the letter shown in the badge, or null to show the moon instead.
 * @param untilText the local time the lock ends, already formatted for the phone's 12/24h setting.
 */
@Composable
fun NightOverlayContent(
    title: String,
    message: String,
    initial: String?,
    untilText: String,
    actionText: String,
    onAction: () -> Unit,
) {
    OrganicTheme {
        val colors = Organic.colors
        val type = Organic.type
        val opacity = Organic.opacity

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.lockedSurface)
                .padding(horizontal = Organic.size.screenGutter, vertical = 30.dp),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Bedtime,
                        contentDescription = null,
                        tint = colors.lockedOnSurface.copy(alpha = opacity.secondary),
                        modifier = Modifier.size(15.dp),
                    )
                    Text(
                        text = "NIGHT LOCK",
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
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(colors.lockedOnSurface.copy(alpha = opacity.faint)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (initial != null) {
                            Text(
                                text = initial,
                                style = type.heading3,
                                color = colors.lockedOnSurface.copy(alpha = opacity.muted),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Bedtime,
                                contentDescription = null,
                                tint = colors.lockedOnSurface.copy(alpha = opacity.muted),
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(top = 20.dp),
                    ) {
                        Text(
                            text = title,
                            style = type.heading2,
                            color = colors.lockedOnSurface,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = message,
                            style = type.bodySmall,
                            color = colors.lockedOnSurface.copy(alpha = opacity.secondary),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "until",
                            style = type.bodySmall,
                            color = colors.lockedOnSurface.copy(alpha = opacity.muted),
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        Text(text = untilText, style = type.heading1, color = colors.accentRamp.c400)
                    }
                }

                PillButton(
                    text = actionText,
                    onClick = onAction,
                    fullWidth = true,
                    containerColor = colors.accent,
                    contentColor = colors.lockedSurface,
                )
            }
        }
    }
}
