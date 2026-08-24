package com.tora.yetanotherpomo.domain.repository

interface AccessibilityStatusChecker {
    fun isServiceEnabled(): Boolean
}
