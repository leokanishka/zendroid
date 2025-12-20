package com.zendroid.launcher.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.zendroid.launcher.domain.InterventionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Accessibility Service for real-time foreground app detection.
 * 
 * CRITICAL MEMORY RULES (Gap A2):
 * - NEVER hold references to AccessibilityNodeInfo beyond onAccessibilityEvent()
 * - Extract primitive data (packageName, className) immediately
 * - Let AccessibilityNodeInfo go out of scope before returning
 */
@AndroidEntryPoint
class ZenDroidAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var interventionManager: InterventionManager

    // Whitelist of system packages to ignore (Gap A3)
    private val whitelistedPackages = setOf(
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher2",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.zendroid.launcher" // Self
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        // Extract primitive data immediately (Gap A2: Memory safety)
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString() ?: ""

        // Ignore whitelisted packages (Gap A3)
        if (isWhitelisted(packageName)) return

        // Delegate to InterventionManager
        interventionManager.evaluateLaunch(packageName, className)
    }

    private fun isWhitelisted(packageName: String): Boolean {
        return whitelistedPackages.any { packageName.startsWith(it) }
    }

    override fun onInterrupt() {
        // Required override - called when accessibility service is interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup if needed
    }
}
