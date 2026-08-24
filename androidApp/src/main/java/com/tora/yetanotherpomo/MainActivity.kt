package com.tora.yetanotherpomo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tora.yetanotherpomo.ui.FocusViewModel
import com.tora.yetanotherpomo.ui.navigation.FocusApp
import com.tora.yetanotherpomo.ui.theme.OrganicTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FocusViewModel = koinViewModel()

            // Re-check accessibility status on every resume - returning from
            // Settings.ACTION_ACCESSIBILITY_SETTINGS naturally triggers ON_RESUME.
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshAccessibilityStatus()
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            OrganicTheme {
                FocusApp(viewModel = viewModel, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
