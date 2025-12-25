package com.zendroid.launcher.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import org.junit.Assert.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)
class ResurrectionWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testResurrectionWorker_runsSuccessfully() {
        val worker = TestWorkerBuilder<ResurrectionWorker>(
            context = context,
            executor = executor
        ).build()

        val result = worker.doWork()
        
        // On emulator/device, if permission is missing, it logs warning and returns success (after posting notification).
        // If permission is present, it starts service and returns success.
        assertThat(result, `is`(ListenableWorker.Result.success()))
    }
}
