package com.tora.yetanotherpomo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.Organic

/**
 * Screen 3/4's "End early?" confirmation — a scrim over the countdown with a bottom-anchored
 * card offering "Keep focusing" (primary) or "End the session" (ghost text), matching the doc's
 * `confirmFocus`/`confirmBlocked` sc-if overlay.
 */
@Composable
fun ConfirmEndSheet(
    bodyText: String,
    onKeepFocusing: () -> Unit,
    onEndSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val type = Organic.type
    val radius = Organic.radius

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.lockedSurface.copy(alpha = Organic.opacity.scrim)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .clip(RoundedCornerShape(radius.lg * 1.15f))
                .background(colors.bg)
                .padding(horizontal = 24.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "End early?", style = type.heading3, color = colors.text)
            Text(
                text = bodyText,
                style = type.body,
                color = colors.neutral.c700,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            PillButton(
                text = "Keep focusing",
                onClick = onKeepFocusing,
                fullWidth = true,
            )
            PillButton(
                text = "End the session",
                onClick = onEndSession,
                fullWidth = true,
                variant = PillButtonVariant.Ghost,
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = colors.neutral.c700,
            )
        }
    }
}
