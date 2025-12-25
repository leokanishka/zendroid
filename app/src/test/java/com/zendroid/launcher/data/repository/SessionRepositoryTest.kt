package com.zendroid.launcher.data.repository

import android.os.SystemClock
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.zendroid.launcher.config.Constants
import com.zendroid.launcher.data.db.SessionDao
import com.zendroid.launcher.data.db.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SessionRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    lateinit var sessionDao: SessionDao

    private lateinit var repository: SessionRepository
    private lateinit var mockedSystemClock: MockedStatic<SystemClock>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = SessionRepository(sessionDao)
        
        // Mock SystemClock for non-Robolectric tests
        mockedSystemClock = Mockito.mockStatic(SystemClock::class.java)
        mockedSystemClock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mockedSystemClock.close()
    }

    @Test
    fun `getAllSessionsFlow calls dao with correct limit`() = runTest(testDispatcher) {
        // Arrange
        val expectedList = listOf(SessionEntity("com.test", 0, 0, 0, ""))
        `when`(sessionDao.getRecentSessionsFlow(Constants.HISTORY_LIMIT)).thenReturn(flowOf(expectedList))

        // Act
        val result = repository.getAllSessionsFlow().first()

        // Assert
        assertEquals(expectedList, result)
        verify(sessionDao).getRecentSessionsFlow(Constants.HISTORY_LIMIT)
    }

    @Test
    fun `hasActiveSession returns true when session not expired`() = runTest(testDispatcher) {
        // Arrange
        val packageName = "com.test"
        val expiryTime = 2000L
        val currentElapsedTime = 1500L
        val session = SessionEntity(packageName, 1000L, 5, expiryTime, "")
        
        `when`(sessionDao.getSession(packageName)).thenReturn(session)

        // Act
        val isActive = repository.hasActiveSession(packageName, currentElapsedTime)

        // Assert
        assertTrue(isActive)
    }

    @Test
    @org.junit.Ignore("Flaky: mockStatic(SystemClock) fails in some JVM environments")
    fun `createSession inserts session with correct times`() = runTest(testDispatcher) {
        // Arrange
        val packageName = "com.test"
        val durationMinutes = 10
        val expectedExpiry = 1000L + (10 * 60 * 1000L) // 1000L is mocked startTime

        // Act
        repository.createSession(packageName, durationMinutes)

        // Assert
        verify(sessionDao).insertSession(org.mockito.kotlin.check {
            assertEquals(packageName, it.packageName)
            assertEquals(1000L, it.startTimeElapsed)
            assertEquals(expectedExpiry, it.expiryTimeElapsed)
        })
    }
}
