package com.zendroid.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zendroid.launcher.data.db.AppDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver for PACKAGE_ADDED/REMOVED events.
 * 
 * Gap D2: Re-apply restrictions when a previously RED app is reinstalled.
 */
@AndroidEntryPoint
class PackageChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appDao: AppDao

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart ?: return

        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                handlePackageAdded(packageName)
            }
            Intent.ACTION_PACKAGE_REMOVED -> {
                handlePackageRemoved(packageName)
            }
        }
    }

    private fun handlePackageAdded(packageName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            // Check if this package was previously tracked
            val existingApp = appDao.getAppByPackage(packageName)
            if (existingApp != null && existingApp.category == "RED") {
                // Package was reinstalled - restrictions still apply
                // No action needed; category is already in DB
                return@launch
            }
            
            // TODO: For new packages, could prompt user to categorize
        }
    }

    private fun handlePackageRemoved(packageName: String) {
        // We don't delete from DB on uninstall - keeps category history
        // This prevents Gap D2 (reinstall to reset limits)
    }
}
