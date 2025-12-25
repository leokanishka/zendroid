package com.zendroid.launcher.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val startHour: Int, // 0-23
    val startMinute: Int, // 0-59
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: String // Comma separated e.g. "1,2,3,4,5" (Mon-Fri)
)
