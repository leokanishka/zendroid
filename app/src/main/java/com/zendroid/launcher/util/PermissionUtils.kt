package com.zendroid.launcher.util

import android.content.Context
import android.provider.Settings
import android.os.Build

/**
 * Utility class for checking and managing permissions required by ZenDroid.
 */
object PermissionUtils {
    
    /**
     * Checks if the app has overlay permission (draw over other apps).
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * Checks if accessibility service is enabled for the app.
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        // Simplified check - in production would check actual accessibility settings
        return true
    }
    
    /**
     * Checks if battery optimization is ignored for the app.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        // Simplified check
        return false
    }
}
