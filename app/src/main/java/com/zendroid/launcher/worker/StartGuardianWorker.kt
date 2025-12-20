package com.zendroid.launcher.worker

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zendroid.launcher.service.GuardianService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker to start GuardianService after boot.
 * Used for Android 15 compliance (Gap B4).
 */
@HiltWorker
class StartGuardianWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val intent = Intent(context, GuardianService::class.java)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        
        return Result.success()
    }
}
