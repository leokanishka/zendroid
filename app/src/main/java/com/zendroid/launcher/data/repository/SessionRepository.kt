package com.zendroid.launcher.data.repository

import android.os.SystemClock
import com.zendroid.launcher.data.db.SessionDao
import com.zendroid.launcher.data.db.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.zendroid.launcher.config.Constants

/**
 * Repository for managing app usage sessions.
 * Uses SystemClock.elapsedRealtime() for all timing (Gap D1).
 */
@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    /**
     * Checks if there's an active (non-expired) session for the given package.
     */
    suspend fun hasActiveSession(packageName: String, currentElapsedTime: Long): Boolean {
        return withContext(Dispatchers.IO) {
            val session = sessionDao.getSession(packageName) ?: return@withContext false
            session.expiryTimeElapsed > currentElapsedTime
        }
    }

    /**
     * Creates a new session for the given package.
     */
    suspend fun createSession(packageName: String, durationMinutes: Int, reason: String = "") {
        withContext(Dispatchers.IO) {
            val currentTime = SystemClock.elapsedRealtime()
            val expiryTime = currentTime + (durationMinutes * 60 * 1000L)
            
            val session = SessionEntity(
                packageName = packageName,
                startTimeElapsed = currentTime,
                durationMinutes = durationMinutes,
                expiryTimeElapsed = expiryTime,
                reason = reason
            )
            sessionDao.insertSession(session)
        }
    }

    /**
     * Extends an existing session by the given minutes.
     */
    suspend fun extendSession(packageName: String, additionalMinutes: Int) {
        withContext(Dispatchers.IO) {
            val session = sessionDao.getSession(packageName) ?: return@withContext
            val newExpiry = session.expiryTimeElapsed + (additionalMinutes * 60 * 1000L)
            val updatedSession = session.copy(
                expiryTimeElapsed = newExpiry,
                durationMinutes = session.durationMinutes + additionalMinutes
            )
            sessionDao.insertSession(updatedSession)
        }
    }

    /**
     * Cleans up expired sessions. Should be called periodically.
     */
    suspend fun cleanupExpiredSessions() {
        withContext(Dispatchers.IO) {
            val currentTime = SystemClock.elapsedRealtime()
            sessionDao.deleteExpiredSessions(currentTime)
        }
    }

    suspend fun clearAllSessions() {
        withContext(Dispatchers.IO) {
            sessionDao.clearAllSessions()
        }
    }

    /**
     * Gets a flow of the total sessions created today.
     * Uses wall-clock time to work correctly across reboots (Gap Fix 2).
     */
    fun getAllSessionsTodayCount(): kotlinx.coroutines.flow.Flow<Int> {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val startOfDayMillis = calendar.timeInMillis
        
        // Use wall-clock time for "today" to survive reboots
        return sessionDao.getSessionCountSinceWallClock(startOfDayMillis)
    }
    
    /**
     * Gets a flow of all sessions for history.
     */
    /**
     * Gets a flow of recent sessions for history (Memory Safe).
     * Hard-capped at 100 items to prevent OOM (Real-World Gap 1).
     */
    fun getAllSessionsFlow(): kotlinx.coroutines.flow.Flow<List<SessionEntity>> {
        return sessionDao.getRecentSessionsFlow(Constants.HISTORY_LIMIT)
    }
}
