package com.tora.yetanotherpomo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.CaprasimoFamily
import com.tora.yetanotherpomo.ui.theme.Organic
import kotlinx.coroutines.launch

/**
 * The doc's "Hold to end" control: an outlined pill whose fill sweeps left-to-right over
 * [holdDurationMs] while pressed, firing [onHoldComplete] at 100%; releasing early cancels and
 * eases the fill back to zero. Ports `startHold`/`holdUp` from the reference prototype 1:1.
 */
@Composable
fun HoldToConfirmButton(
    text: String,
    holdDurationMs: Int,
    onHoldComplete: () -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 56.dp,
    borderColor: Color = Organic.colors.lockedOnSurface.copy(alpha = Organic.opacity.border),
    fillColor: Color = Organic.colors.accentRamp.c800,
    contentColor: Color = Organic.colors.lockedOnSurface.copy(alpha = Organic.opacity.strong),
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(percent = 50)
    val motion = Organic.motion
    val borderWidth = Organic.stroke.hairline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .pointerInput(holdDurationMs) {
                detectTapGestures(
                    onPress = {
                        val holdJob = scope.launch {
                            progress.snapTo(0f)
                            progress.animateTo(1f, tween(holdDurationMs, easing = motion.linear))
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onHoldComplete()
                        }
                        tryAwaitRelease()
                        holdJob.cancel()
                        scope.launch { progress.animateTo(0f, tween(motion.quick)) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = progress.value.coerceIn(0f, 1f))
                .background(fillColor),
        )
        Text(
            text = text,
            fontFamily = CaprasimoFamily,
            color = contentColor,
        )
    }
}
