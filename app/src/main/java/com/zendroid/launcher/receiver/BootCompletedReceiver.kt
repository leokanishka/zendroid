package com.zendroid.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zendroid.launcher.service.GuardianService
import com.zendroid.launcher.worker.StartGuardianWorker

/**
 * Receiver for BOOT_COMPLETED to restore sessions and restart Guardian.
 * 
 * Android 15 Compliance (Gap B4):
 * - Cannot directly start certain FGS types from BOOT_COMPLETED
 * - Uses WorkManager to defer startup after device unlock
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            
            // Use WorkManager to start Guardian after device is ready (Gap B4)
            val workRequest = OneTimeWorkRequestBuilder<StartGuardianWorker>()
                .build()
            
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
