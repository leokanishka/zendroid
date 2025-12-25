package com.zendroid.launcher.util

import android.util.Log

/**
 * Wrapper for crash reporting.
 * Currently uses Logcat, but ready for Firebase Crashlytics integration.
 *
 * To enable Firebase:
 * 1. Add google-services.json to /app
 * 2. Uncomment Firebase dependencies in build.gradle.kts
 * 3. Uncomment Firebase code in this class
 */
object CrashReporter {
    fun logException(e: Throwable) {
        // Placeholder for Firebase Crashlytics
        // FirebaseCrashlytics.getInstance().recordException(e)
        Log.e("CrashReporter", "Non-fatal error caught", e)
    }

    fun log(message: String) {
        // FirebaseCrashlytics.getInstance().log(message)
        Log.d("CrashReporter", message)
    }
}
