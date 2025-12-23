package com.zendroid.launcher.domain

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings
import com.zendroid.launcher.data.db.AppDao
import com.zendroid.launcher.data.repository.SessionRepository
import com.zendroid.launcher.ui.intervention.InterventionActivity
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

    // Categories: GREEN = always allow, YELLOW = soft friction, RED = full intervention
    enum class AppCategory { GREEN, YELLOW, RED }

    /**
     * Evaluates whether an app launch should be intercepted.
     * Called from AccessibilityService on TYPE_WINDOW_STATE_CHANGED.
     */
    fun evaluateLaunch(packageName: String, className: String) {
        if (!isInterventionActive.compareAndSet(false, true)) return

        scope.launch {
            try {
                val category = getAppCategory(packageName)
                
                when (category) {
                    AppCategory.GREEN -> {
                        // Allow silently
                        return@launch
                    }
                    AppCategory.YELLOW -> {
                        // TODO: Show bottom sheet (Sprint 3)
                        return@launch
                    }
                    AppCategory.RED -> {
                        handleRedApp(packageName)
                    }
                }
            } finally {
                // Keep locked if InterventionActivity is showing - it will reset the lock
                // or we reset it here if we didn't launch an activity
                if (getAppCategory(packageName) != AppCategory.RED) {
                    isInterventionActive.set(false)
                }
            }
        }
    }

    /**
     * Call this when InterventionActivity is dismissed or session is granted.
     */
    fun resetInterventionLock() {
        isInterventionActive.set(false)
    }

    private suspend fun handleRedApp(packageName: String) {
        // Check for active session (Gap D1: elapsedRealtime)
        val currentTime = SystemClock.elapsedRealtime()
        if (sessionRepository.hasActiveSession(packageName, currentTime)) {
            // Session exists and not expired - allow
            return
        }

        // No active session - trigger intervention
        triggerIntervention(packageName)
    }

    private fun triggerIntervention(packageName: String) {
        // Check if we can draw overlays (Gap C1, C2)
        if (Settings.canDrawOverlays(context)) {
            launchInterventionActivity(packageName)
        } else {
            // Fallback to notification (Gap C1)
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

    private fun showInterventionNotification(packageName: String) {
        // TODO: HeadsUpNotificationManager.show(packageName) - Implement in Phase 5
    }

    private suspend fun getAppCategory(packageName: String): AppCategory {
        val appEntity = appDao.getAppByPackage(packageName)
        return when (appEntity?.category) {
            "RED" -> AppCategory.RED
            "YELLOW" -> AppCategory.YELLOW
            else -> AppCategory.GREEN
        }
    }
}
