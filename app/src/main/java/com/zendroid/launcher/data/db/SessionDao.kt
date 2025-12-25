package com.zendroid.launcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE packageName = :packageName LIMIT 1")
    suspend fun getSession(packageName: String): SessionEntity?

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE packageName = :packageName")
    suspend fun deleteSession(packageName: String)

    @Query("DELETE FROM sessions WHERE expiryTimeElapsed < :currentElapsedTime")
    suspend fun deleteExpiredSessions(currentElapsedTime: Long)

    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()

    @Query("SELECT COUNT(*) FROM sessions WHERE startTimeElapsed > :sinceTime")
    fun getSessionCountSince(sinceTime: Long): Flow<Int>

    // Gap Fix 2: Wall-clock time query for "today" that works across reboots
    @Query("SELECT COUNT(*) FROM sessions WHERE createdAtMillis >= :sinceWallClockMillis")
    fun getSessionCountSinceWallClock(sinceWallClockMillis: Long): Flow<Int>

    // Order by wall‑clock timestamp to keep history chronological across device reboots
    @Query("SELECT * FROM sessions ORDER BY createdAtMillis DESC")
    fun getAllSessionsFlow(): Flow<List<SessionEntity>>

    // Real-World Gap 1: Memory-Safe Pagination
    @Query("SELECT * FROM sessions ORDER BY startTimeElapsed DESC LIMIT :limit")
    fun getRecentSessionsFlow(limit: Int): Flow<List<SessionEntity>>
}
