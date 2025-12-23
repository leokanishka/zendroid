package com.zendroid.launcher.data.repository

import android.os.SystemClock
import com.zendroid.launcher.data.db.SessionDao
import com.zendroid.launcher.data.db.SessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SessionRepositoryTest {

    @Mock
    lateinit var sessionDao: SessionDao

    lateinit var repository: SessionRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = SessionRepository(sessionDao)
    }

    @Test
    fun `hasActiveSession returns true if session exists and not expired`() = runTest {
        val packageName = "com.test.app"
        val currentTime = 100000L
        val session = SessionEntity(
            packageName = packageName,
            startTimeElapsed = currentTime - 1000,
            durationMinutes = 5,
            expiryTimeElapsed = currentTime + 5000,
            reason = "Test"
        )
        
        `when`(sessionDao.getSession(packageName)).thenReturn(session)
        
        val result = repository.hasActiveSession(packageName, currentTime)
        assertTrue(result)
    }

    @Test
    fun `hasActiveSession returns false if session expired`() = runTest {
        val packageName = "com.test.app"
        val currentTime = 100000L
        val session = SessionEntity(
            packageName = packageName,
            startTimeElapsed = currentTime - 6000,
            durationMinutes = 5,
            expiryTimeElapsed = currentTime - 1000,
            reason = "Test"
        )
        
        `when`(sessionDao.getSession(packageName)).thenReturn(session)
        
        val result = repository.hasActiveSession(packageName, currentTime)
        assertFalse(result)
    }

    @Test
    fun `createSession calls dao insert`() = runTest {
        val packageName = "com.test.app"
        repository.createSession(packageName, 5, "Bored")
        
        verify(sessionDao).insertSession(any(SessionEntity::class.java))
    }
}
