package com.zendroid.launcher.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AppEntity::class, SessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    abstract fun sessionDao(): SessionDao
}
