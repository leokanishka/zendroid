package com.zendroid.launcher.config

/**
 * Centralised constants for the ZenDroid application.
 * Keeping magic numbers in one place makes it easy to tune the app for
 * different devices and to avoid accidental duplication.
 */
object Constants {
    /** Maximum number of recent sessions shown in the History screen. */
    const val HISTORY_LIMIT = 100

    /** Interval (in minutes) for the ResurrectionWorker periodic work. */
    const val RESURRECTION_INTERVAL_MINUTES = 15L

    /** Minimum number of icons to keep in the cache (memory‑constrained devices). */
    const val ICON_CACHE_MIN_ICONS = 20

    /** Maximum number of icons to keep in the cache (high‑end devices). */
    const val ICON_CACHE_MAX_ICONS = 100
}
