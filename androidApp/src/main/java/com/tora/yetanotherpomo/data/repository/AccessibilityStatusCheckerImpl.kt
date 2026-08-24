package com.tora.yetanotherpomo.data.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.tora.yetanotherpomo.domain.repository.AccessibilityStatusChecker
import com.tora.yetanotherpomo.service.FocusAccessibilityService

class AccessibilityStatusCheckerImpl(
    private val context: Context,
) : AccessibilityStatusChecker {

    private val targetComponent = ComponentName(context.packageName, FocusAccessibilityService::class.java.name)

    override fun isServiceEnabled(): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabledServices.any { info ->
            val resolved = ComponentName(info.resolveInfo.serviceInfo.packageName, info.resolveInfo.serviceInfo.name)
            resolved == targetComponent
        }
    }
}
