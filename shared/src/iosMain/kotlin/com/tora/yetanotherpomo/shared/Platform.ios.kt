package com.tora.yetanotherpomo.shared

import platform.UIKit.UIDevice

// Kotlin/Native ships generated bindings for the Apple SDKs. `platform.UIKit` here is literally
// Apple's UIKit, callable as Kotlin - no Swift, no bridging code written by hand.
actual fun platformName(): String =
    UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
