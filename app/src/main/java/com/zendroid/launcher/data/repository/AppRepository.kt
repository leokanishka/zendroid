package com.zendroid.launcher.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.zendroid.launcher.data.db.AppDao
import com.zendroid.launcher.data.db.AppEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val appDao: AppDao,
    @ApplicationContext private val context: Context
) {
    val apps: Flow<List<AppEntity>> = appDao.getAllApps()

    suspend fun syncApps() = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val activePackages = resolveInfos.map { it.activityInfo.packageName }.toSet()
        val entities = resolveInfos.map { resolveInfo ->
            val packageName = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(pm).toString()
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            
            AppEntity(
                packageName = packageName,
                label = label,
                isSystem = isSystem,
                category = if (isSystem) "GREEN" else "YELLOW" // Default heuristic
            )
        }

        // 1. Insert/Update existing
        appDao.insertApps(entities)
        
        // 2. Remove uninstalled
        appDao.deleteStartledApps(activePackages.toList())
    }
    suspend fun updateAppCategory(packageName: String, category: String) {
        withContext(Dispatchers.IO) {
            appDao.updateCategory(packageName, category)
        }
    }
}
