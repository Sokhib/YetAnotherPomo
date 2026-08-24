package com.tora.yetanotherpomo.shared

import android.os.Build

// `actual` must match the `expect` signature exactly. In return, this file may touch android.*,
// which commonMain cannot even see.
actual fun platformName(): String = "Android ${Build.VERSION.SDK_INT}"
