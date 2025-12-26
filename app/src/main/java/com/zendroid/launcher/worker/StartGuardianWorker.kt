package com.zendroid.launcher.worker

import android.content.Context
import android.content.Intent
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
    @Assisted workerParams: WorkerParameters,
    private val sessionRepository: com.zendroid.launcher.data.repository.SessionRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Handle Boot Cleanup (Fix for Gap D1: elapsedRealtime reset)
        if (inputData.getString("action") == "boot_cleanup") {
            sessionRepository.clearAllSessions()
        }

        val intent = Intent(context, GuardianService::class.java)
        context.startForegroundService(intent)
        
        return Result.success()
    }
}
