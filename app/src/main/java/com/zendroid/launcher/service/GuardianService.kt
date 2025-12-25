package com.zendroid.launcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.zendroid.launcher.MainActivity
import com.zendroid.launcher.R
import com.zendroid.launcher.util.CrashReporter
import com.zendroid.launcher.data.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Guardian Foreground Service for:
 * 1. Keeping ZenDroid alive against OEM battery killers (Gap B1)
 * 2. Periodically checking Accessibility status (Gap 8.1)
 * 3. Cleaning up expired sessions
 * 
 * Uses foregroundServiceType="specialUse" for Android 15 compliance (Gap B3).
 */
@AndroidEntryPoint
class GuardianService : Service() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var interventionManager: com.zendroid.launcher.domain.InterventionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var watchdogJob: Job? = null

    companion object {
        const val CHANNEL_ID = "zendroid_guardian"
        const val NOTIFICATION_ID = 1001
        private const val WATCHDOG_INTERVAL_MS = 30_000L // 30 seconds
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startWatchdog()
        return START_STICKY // Restart if killed (Gap B1)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Self-restart when swiped from recents (Gap B1)
        val restartIntent = Intent(applicationContext, GuardianService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }

    override fun onDestroy() {
        watchdogJob?.cancel()
        super.onDestroy()
    }

    private fun startWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = serviceScope.launch {
            while (isActive) {
                performWatchdogChecks()
                delay(WATCHDOG_INTERVAL_MS)
            }
        }
    }

    private suspend fun performWatchdogChecks() {
        try {
            // 1. Cleanup expired sessions
            sessionRepository.cleanupExpiredSessions()
            
            // 2. Check if Accessibility Service is still enabled
            if (!isAccessibilityServiceEnabled()) {
                showShieldDownNotification()
            }

            // 3. Stuck Lock Recovery (Gap B1: Resilience)
            checkInterventionLockHealth()
        } catch (e: Exception) {
            CrashReporter.logException(e)
        }
    }

    private var lockDetectedCount = 0
    private fun checkInterventionLockHealth() {
        // Gap Fix 2: Smart Watchdog
        // Only reset if lock is held for > 60 seconds (prevents interrupting breathing)
        if (interventionManager.isLockStuck(60_000L)) {
            lockDetectedCount++
            if (lockDetectedCount >= 1) { // Immediate reset on detection if > 60s
                interventionManager.forceResetLock() 
                lockDetectedCount = 0
            }
        } else {
            lockDetectedCount = 0
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        
        // Real-World Gap 2: Robust Check
        // System can store as "com.pkg/.Service" OR "com.pkg/com.pkg.Service"
        val expectedServiceName = ZenDroidAccessibilityService::class.java.simpleName
        
        val colonSplit = android.text.TextUtils.SimpleStringSplitter(':')
        colonSplit.setString(enabledServices)
        
        while (colonSplit.hasNext()) {
            val componentName = colonSplit.next()
            if (componentName.contains(packageName) && 
                componentName.contains(expectedServiceName)) {
                return true
            }
        }
        return false
    }

    private fun showShieldDownNotification() {
        val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.shield_down_title))
            .setContentText(getString(R.string.shield_down_text))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.guardian_notification_title))
            .setContentText(getString(R.string.guardian_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ZenDroid Guardian",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps ZenDroid running to protect your focus"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }
}
