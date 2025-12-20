package com.zendroid.launcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}
