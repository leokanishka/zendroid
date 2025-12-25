package com.zendroid.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zendroid.launcher.ui.home.HomeScreen
import com.zendroid.launcher.ui.history.HistoryScreen
import dagger.hilt.android.AndroidEntryPoint

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.zendroid.launcher.ui.dialogs.OverlayPermissionDialog

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var showOverlayDialog by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        checkOverlayPermission()
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showOverlayDialog = !Settings.canDrawOverlays(this)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(onOpenHistory = { navController.navigate("history") })
                }
                composable("history") {
                    HistoryScreen(onBack = { navController.popBackStack() })
                }
            }
            if (showOverlayDialog) {
                OverlayPermissionDialog(
                    onDismiss = { showOverlayDialog = false },
                    onConfirm = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                        startActivity(intent)
                        showOverlayDialog = false
                    }
                )
            }
        }
    }


}
