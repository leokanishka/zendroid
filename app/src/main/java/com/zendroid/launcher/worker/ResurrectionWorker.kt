package com.zendroid.launcher.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import android.provider.Settings
import android.net.Uri
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zendroid.launcher.service.GuardianService

/**
 * Project Lazarus: Periodically checks and restarts the GuardianService.
 *
 * Addresses the OEM "Zombie" risk by ensuring the foreground service stays alive.
 * Runs every 15 minutes (minimum interval allowed by WorkManager).
 *
 * **Robustness notes**
 * 1. **Overlay permission** – on Android 12+ we need the `SYSTEM_ALERT_WINDOW`
 *    permission to start a foreground service from the background. The worker now
 *    checks `Settings.canDrawOverlays()` before attempting the start.
 * 2. **Idempotent start** – calling `startForegroundService` on an already
 *    running service is safe; the service simply receives a new intent.
 * 3. **No race conditions** – the worker runs on a background thread and the
 *    service handles its own synchronization.
 */
class ResurrectionWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            restartGuardian()
            Result.success()
        } catch (e: Exception) {
            // Keep retrying if we fail, this is critical
            Result.retry()
        }
    }

    private fun restartGuardian() {
        val intent = Intent(applicationContext, GuardianService::class.java)
        
        // Android 8.0+ requires startForegroundService for FGS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Settings.canDrawOverlays(applicationContext)) {
                try {
                    // Overlay permission granted – safe to start foreground service
                    applicationContext.startForegroundService(intent)
                } catch (e: Exception) {
                    Log.e("ResurrectionWorker", "Failed to resurrect Guardian", e)
                    throw e // Retry
                }
            } else {
                Log.w("ResurrectionWorker", "Overlay permission missing; cannot start GuardianService from background.")
                // No retry now – the periodic worker will run again later.
                showOverlayPermissionNotification()
            }
        } else {
            applicationContext.startService(intent)
        }
    }


    private fun showOverlayPermissionNotification() {
        val channelId = "overlay_permission_channel"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Overlay Permission",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Required to keep your digital shield active"
            }
            nm.createNotificationChannel(channel)
        }

        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = Uri.parse("package:${applicationContext.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 
            0, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Use a system icon or app icon
            .setContentTitle("Shield Permission Missing")
            .setContentText("Tap to allow 'Draw over other apps' to keep ZenDroid active.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        nm.notify(999, notification)
    }
}
