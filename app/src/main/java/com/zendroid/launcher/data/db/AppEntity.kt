package com.zendroid.launcher.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val category: String, // "GREEN", "YELLOW", "RED"
    val usageCount: Int = 0,
    val lastOpened: Long = 0,
    val isHidden: Boolean = false
)
