package com.zendroid.launcher.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an active usage session for a "Red" app.
 * Uses elapsedRealtime for timing (Gap D1: Clock manipulation prevention).
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val packageName: String,
    val startTimeElapsed: Long,    // SystemClock.elapsedRealtime() at session start
    val durationMinutes: Int,
    val expiryTimeElapsed: Long,   // startTimeElapsed + (durationMinutes * 60 * 1000)
    val reason: String = ""        // User's stated reason for using the app
)
