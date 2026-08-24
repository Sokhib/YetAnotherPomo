package com.tora.yetanotherpomo.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Rasterisation size for a cached launcher icon - covers the 42.dp allowlist avatar at 3x density. */
private const val IconSizePx = 128

/**
 * A process-wide cache of rasterised launcher icons.
 *
 * `PackageManager.getApplicationIcon` is a binder call that also inflates an (often adaptive)
 * drawable, and turning that into a bitmap is another chunk of work. Doing both inside composition
 * stalls the frame, and because LazyColumn drops a row's `remember` the moment it scrolls off
 * screen, every fling paid the cost again for the same handful of packages. Holding the bitmaps
 * here means each package is resolved once, off the main thread, and re-scrolling is free.
 */
object AppIconCache {

    /** Marks a package we already tried and failed to resolve, so we do not retry it every fling. */
    private val NoIcon = Any()

    private val cache = object : LruCache<String, Any>(maxSizeKb()) {
        override fun sizeOf(key: String, value: Any): Int =
            if (value is ImageBitmap) value.asAndroidBitmap().allocationByteCount / 1024 else 1
    }

    /** The already-resolved icon, or null when this package has not been rasterised yet. */
    fun peek(packageName: String): ImageBitmap? = cache[packageName] as? ImageBitmap

    /** Resolves and caches the icon off the main thread. Returns null when the package has none. */
    suspend fun load(context: Context, packageName: String): ImageBitmap? {
        cache[packageName]?.let { return it as? ImageBitmap }
        val icon = withContext(Dispatchers.Default) {
            runCatching { context.packageManager.getApplicationIcon(packageName) }
                .getOrNull()
                ?.let { rasterise(it) }
        }
        cache.put(packageName, icon ?: NoIcon)
        return icon
    }

    private fun rasterise(drawable: Drawable): ImageBitmap? {
        if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) return null
        val bitmap = Bitmap.createBitmap(IconSizePx, IconSizePx, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, IconSizePx, IconSizePx)
        drawable.draw(Canvas(bitmap))
        return bitmap.asImageBitmap()
    }

    private fun maxSizeKb(): Int =
        (Runtime.getRuntime().maxMemory() / 1024 / 8).coerceAtMost(4096).toInt()
}

/**
 * The launcher icon for [packageName], or null until it has been resolved. Already-cached packages
 * are returned on the first composition, so a row that scrolls back into view never flashes its
 * placeholder.
 */
@Composable
fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current.applicationContext
    return produceState(initialValue = AppIconCache.peek(packageName), packageName, context) {
        if (value == null) value = AppIconCache.load(context, packageName)
    }.value
}
