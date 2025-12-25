package com.zendroid.launcher.domain

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import com.zendroid.launcher.data.db.AppDao
import com.zendroid.launcher.data.repository.SessionRepository
import com.zendroid.launcher.ui.intervention.InterventionActivity
import com.zendroid.launcher.ui.intervention.YellowInterventionActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core decision engine for app interventions.
 * 
 * Implements Gap D1: Uses SystemClock.elapsedRealtime() for all timing
 * Implements Gap C1: Falls back to notification if overlay denied
 */
@Singleton
class InterventionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appDao: AppDao,
    private val sessionRepository: SessionRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val isInterventionActive = AtomicBoolean(false)
    
    // Performance: Reactive Cache for O(1) lookups during high-frequency events
    private var categoryCache = emptyMap<String, AppCategory>()
    
    // Performance: Reactive Cache for Schedules (Gap Fix 1)
    private var scheduleCache = emptyList<com.zendroid.launcher.data.db.ScheduleEntity>()

    // Safety: Intent Debouncing
    private var lastPackageName: String? = null
    private var lastEventTime: Long = 0
    
    // Safety: Lock Timestamp (Gap Fix 2)
    private var lockTimestamp: Long = 0

    init {
        // Observe categories in real-time to keep cache hot (Gap A2: Performance)
        scope.launch {
            appDao.getAllApps().collect { apps ->
                categoryCache = apps.associate { it.packageName to it.category.toAppCategory() }
            }
        }
        // Observe schedules in real-time (Gap Fix 1)
        scope.launch {
            com.zendroid.launcher.data.db.AppDatabase.getInstance(context).scheduleDao().getAllSchedulesFlow().collect { schedules ->
                scheduleCache = schedules
            }
        }
    }

    private fun String?.toAppCategory(): AppCategory = when (this) {
        "RED" -> AppCategory.RED
        "YELLOW" -> AppCategory.YELLOW
        else -> AppCategory.GREEN
    }

    // Categories: GREEN = always allow, YELLOW = soft friction, RED = full intervention
    enum class AppCategory { GREEN, YELLOW, RED }

    /**
     * Evaluates whether an app launch should be intercepted.
     * Called from AccessibilityService on TYPE_WINDOW_STATE_CHANGED.
     */
    fun evaluateLaunch(packageName: String, className: String) {
        val currentTime = SystemClock.elapsedRealtime()
        
        // Anti-Jitter: Ignore repeat events within 200ms for the same package
        if (packageName == lastPackageName && (currentTime - lastEventTime) < 200) {
            return
        }
        lastPackageName = packageName
        lastEventTime = currentTime

        if (isInterventionActive.get()) return

        // Anti-Evasion: Intercept Settings (Gap B2: Persistent Protection)
        if (packageName == "com.android.settings") {
            handleSettingsEvasion(className)
            return
        }

        scope.launch {
            try {
                // O(1) Cache Lookup (Max performance during window shifts)
                val category = categoryCache[packageName] ?: AppCategory.GREEN
                
                // Focus Profile (Gap C1): Check for active schedules
                val isFocusProfileActive = isAnyFocusProfileActive()
                
                val effectiveCategory = when {
                    category == AppCategory.RED -> AppCategory.RED
                    category == AppCategory.YELLOW && isFocusProfileActive -> AppCategory.RED // Upgrade YELLOW to RED
                    else -> category
                }

                when (effectiveCategory) {
                    AppCategory.GREEN -> return@launch
                    AppCategory.YELLOW -> {
                        if (acquireLock()) {
                            launchYellowIntervention(packageName)
                        }
                    }
                    AppCategory.RED -> {
                        if (handleRedAppCheck(packageName)) {
                           if (acquireLock()) {
                               triggerIntervention(packageName)
                           }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fail-safe: release lock on error
                resetInterventionLock()
            }
        }
    }
    
    private fun acquireLock(): Boolean {
        if (isInterventionActive.compareAndSet(false, true)) {
            lockTimestamp = SystemClock.elapsedRealtime()
            return true
        }
        return false
    }

    /**
     * Call this when InterventionActivity is dismissed or session is granted.
     */
    fun resetInterventionLock() {
        isInterventionActive.set(false)
    }

    /**
     * Debug/Safety: Checks if lock is stuck (held longer than timeout).
     */
    fun isLockStuck(timeoutMs: Long): Boolean {
        if (!isInterventionActive.get()) return false
        val elapsed = SystemClock.elapsedRealtime() - lockTimestamp
        return elapsed > timeoutMs
    }

    /**
     * Debug/Safety: Force reset the lock from Watchdog.
     */
    fun forceResetLock() {
        isInterventionActive.set(false)
        lockTimestamp = 0
    }

    private suspend fun handleRedAppCheck(packageName: String): Boolean {
        // Check for active session (Gap D1: elapsedRealtime)
        val currentTime = SystemClock.elapsedRealtime()
        return !sessionRepository.hasActiveSession(packageName, currentTime)
    }

    private fun triggerIntervention(packageName: String) {
        // Check if we can draw overlays (Gap C1, C2)
        if (Settings.canDrawOverlays(context)) {
            launchInterventionActivity(packageName)
        } else {
            // Fallback to high-priority notification (Gap C1)
            showInterventionNotification(packageName)
        }
    }

    private fun launchInterventionActivity(packageName: String) {
        val intent = Intent(context, InterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(InterventionActivity.EXTRA_TARGET_PACKAGE, packageName)
        }
        context.startActivity(intent)
    }

    private fun launchYellowIntervention(packageName: String) {
        val intent = Intent(context, YellowInterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(YellowInterventionActivity.EXTRA_TARGET_PACKAGE, packageName)
        }
        context.startActivity(intent)
    }

    private fun isAnyFocusProfileActive(): Boolean {
        // Use cached list instead of DB query
        val schedules = scheduleCache
        val now = java.util.Calendar.getInstance()
        val currentHour = now.get(java.util.Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(java.util.Calendar.MINUTE)
        val currentDay = now.get(java.util.Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
        
        return schedules.any { schedule ->
            if (!schedule.isEnabled) return@any false
            
            // Day check with safe parsing
            val days = if (schedule.daysOfWeek.isBlank()) {
                emptyList<Int>()
            } else {
                schedule.daysOfWeek.split(",").mapNotNull { part ->
                    val trimmed = part.trim()
                    try {
                        trimmed.toInt()
                    } catch (e: Exception) {
                        // Invalid entry, ignore it
                        null
                    }
                }
            }
            if (days.isEmpty() || currentDay !in days) return@any false
            
            val nowMinutes = currentHour * 60 + currentMinute
            val startMinutes = schedule.startHour * 60 + schedule.startMinute
            val endMinutes = schedule.endHour * 60 + schedule.endMinute
            
            if (startMinutes <= endMinutes) {
                nowMinutes in startMinutes..endMinutes
            } else {
                // Crosses midnight (e.g., 22:00 to 07:00)
                nowMinutes >= startMinutes || nowMinutes <= endMinutes
            }
        }
    }

    private fun handleSettingsEvasion(className: String) {
        // We look for "Accessibility" or "AppInfo" (Specific to ZenDroid)
        val isSensitive = className.contains("Accessibility", ignoreCase = true) || 
                         className.contains("AppInfo", ignoreCase = true)
        
        if (isSensitive) {
            if (isInterventionActive.compareAndSet(false, true)) {
                // High friction for settings
                launchInterventionActivity("com.android.settings")
            }
        }
    }

    private fun showInterventionNotification(packageName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "zendroid_intervention"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "ZenDroid Interventions",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when an app is restricted but overlay is blocked"
            }
            nm.createNotificationChannel(channel)
        }

        // Tap to open ZenDroid and fix permissions
        val intent = Intent(context, com.zendroid.launcher.ui.settings.SettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, 0, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setContentTitle("Access Restricted")
            .setContentText("ZenDroid cannot display over $packageName. Tap to fix permissions.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        nm.notify(1002, notification)
    }
}
