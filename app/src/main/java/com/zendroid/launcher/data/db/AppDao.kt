package com.zendroid.launcher.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY label ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("DELETE FROM apps WHERE packageName NOT IN (:activePackageNames)")
    suspend fun deleteStartledApps(activePackageNames: List<String>)
    
    @Query("UPDATE apps SET usageCount = usageCount + 1, lastOpened = :timestamp WHERE packageName = :packageName")
    suspend fun recordUsage(packageName: String, timestamp: Long)

    @Query("SELECT * FROM apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackage(packageName: String): AppEntity?

    @Query("UPDATE apps SET category = :category WHERE packageName = :packageName")
    suspend fun updateCategory(packageName: String, category: String)
}
