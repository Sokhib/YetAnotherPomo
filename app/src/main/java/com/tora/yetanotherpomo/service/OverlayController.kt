package com.tora.yetanotherpomo.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView

private const val TAG = "OverlayController"

/**
 * A Compose host that swallows the Back key so it can't dismiss the block overlay. Extends
 * [AbstractComposeView] (not the final [androidx.compose.ui.platform.ComposeView]) with the
 * content fixed at construction time and rendered via [Content].
 */
private class BackAwareComposeView(
    context: Context,
    private val content: @Composable () -> Unit,
) : AbstractComposeView(context) {
    init {
        // A window with no FLAG_NOT_FOCUSABLE is *eligible* for input focus, but the root view
        // still needs to be focusable and actually claim it - otherwise dispatchKeyEvent below
        // is never called and Back silently falls through to whatever is underneath.
        isFocusable = true
        isFocusableInTouchMode = true
    }

    @Composable
    override fun Content() {
        content()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) return true
        return super.dispatchKeyEvent(event)
    }
}

/**
 * Owns the single [TYPE_ACCESSIBILITY_OVERLAY][WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY]
 * window a bound AccessibilityService may show. That window type needs no `SYSTEM_ALERT_WINDOW`
 * permission - it exists only while the service is bound. [show]/[hide] fully construct and tear
 * down the [OverlayLifecycleOwner] + [AbstractComposeView] pair each time (never kept alive
 * paused between shows) to avoid stale hold-to-end animation state leaking into the next display.
 */
class OverlayController(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var view: AbstractComposeView? = null
    private var owner: OverlayLifecycleOwner? = null

    val isShowing: Boolean get() = view != null

    /**
     * @param backgroundColor painted by the [android.view.View] layer itself, so the very first
     * frame after `addView` is already opaque. Without it the window is transparent until the
     * first composition has measured and drawn, and the blocked app shows through for a frame or
     * two - the visible "blink" before the block lands. Must match the content's own background.
     */
    fun show(backgroundColor: Int, content: @Composable () -> Unit) {
        if (isShowing) return

        val newOwner = OverlayLifecycleOwner()
        val newView = BackAwareComposeView(context, content)
        newView.setBackgroundColor(backgroundColor)

        newOwner.create()
        newOwner.attachTo(newView)
        newOwner.start()
        newOwner.resume()

        try {
            windowManager.addView(newView, buildLayoutParams())
            newView.requestFocus()
            view = newView
            owner = newOwner
        } catch (e: WindowManager.BadTokenException) {
            Log.w(TAG, "Failed to add overlay window", e)
            newOwner.destroy()
        }
    }

    fun hide() {
        val currentView = view ?: return
        val currentOwner = owner
        view = null
        owner = null

        currentOwner?.pause()
        try {
            windowManager.removeViewImmediate(currentView)
        } catch (e: WindowManager.BadTokenException) {
            Log.w(TAG, "Overlay window already gone", e)
        }
        currentOwner?.stop()
        currentOwner?.destroy()
    }

    /** Idempotent - safe to call from onInterrupt/onUnbind/onDestroy regardless of state. */
    fun teardown() {
        hide()
    }

    private fun buildLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        return params
    }
}
