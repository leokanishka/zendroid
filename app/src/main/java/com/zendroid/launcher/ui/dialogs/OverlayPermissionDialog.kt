package com.zendroid.launcher.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun OverlayPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("To ensure ZenDroid stays active and protects your focus, please allow 'Draw over other apps' in Settings.") },
        confirmButton = { 
            Button(onClick = onConfirm) { 
                Text("Allow") 
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Not Now") 
            } 
        }
    )
}
