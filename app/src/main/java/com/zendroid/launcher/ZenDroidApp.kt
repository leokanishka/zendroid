package com.zendroid.launcher

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.zendroid.launcher.config.Constants
import com.zendroid.launcher.worker.ResurrectionWorker
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class ZenDroidApp : Application() {
    override fun onCreate() {
        super.onCreate()
        scheduleGuardianResurrection()
    }

    private fun scheduleGuardianResurrection() {
        val workRequest = PeriodicWorkRequestBuilder<ResurrectionWorker>(
            Constants.RESURRECTION_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "guardian_resurrection",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
