package com.zendroid.launcher.util

import android.util.Log

object PerformanceMonitor {
    private const val TAG = "ZenDroidPerformance"
    private const val THRESHOLD_MS = 100

    fun trace(name: String, block: () -> Unit) {
        val start = System.currentTimeMillis()
        block()
        val end = System.currentTimeMillis()
        val duration = end - start

        if (duration > THRESHOLD_MS) {
            Log.w(TAG, "SLOW RENDER: $name took ${duration}ms")
            // In Alpha 25, this would be written to a local file
        }
    }
}
