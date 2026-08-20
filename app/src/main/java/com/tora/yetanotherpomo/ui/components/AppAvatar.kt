package com.tora.yetanotherpomo.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.tora.yetanotherpomo.ui.theme.CaprasimoFamily
import com.tora.yetanotherpomo.ui.theme.Organic

/** Alternating accent/accent2 tone for a list of apps, mirroring the doc's `tone(i)` helper. */
@Composable
fun appAvatarTone(index: Int): Pair<Color, Color> {
    val colors = Organic.colors
    return if (index % 2 == 1) {
        colors.accent2Ramp.c200 to colors.accent2Ramp.c800
    } else {
        colors.accentRamp.c200 to colors.accentRamp.c800
    }
}

/**
 * A circular avatar for an installed app: the real launcher icon when it can be resolved,
 * otherwise an initial-letter placeholder on the given tone (the doc's own fallback treatment
 * for its mock app tiles).
 */
@Composable
fun AppAvatar(
    packageName: String,
    label: String,
    size: Dp,
    toneBg: Color,
    toneFg: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        runCatching { context.packageManager.getApplicationIcon(packageName) }
            .getOrNull()
            ?.let { drawableToImageBitmap(it, 96) }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(toneBg),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size),
            )
        } else {
            Text(
                text = label.take(1).uppercase(),
                color = toneFg,
                fontFamily = CaprasimoFamily,
            )
        }
    }
}

private fun drawableToImageBitmap(drawable: Drawable, sizePx: Int): androidx.compose.ui.graphics.ImageBitmap? {
    if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return null
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, sizePx, sizePx)
    drawable.draw(canvas)
    return bitmap.asImageBitmap()
}
