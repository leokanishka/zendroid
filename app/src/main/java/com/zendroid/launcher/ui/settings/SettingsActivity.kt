package com.zendroid.launcher.ui.settings

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zendroid.launcher.data.repository.SettingsRepository
import com.zendroid.launcher.ui.intervention.friction.FrictionEngine
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SettingsScreen(settingsRepository)
        }
    }
}

@Composable
fun SettingsScreen(repository: SettingsRepository) {
    val context = LocalContext.current
    var isProtectionEnabled by remember { mutableStateOf(repository.isProtectionEnabled()) }
    var frictionLevel by remember { mutableStateOf(repository.getFrictionLevel()) }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(title = { Text("ZenDroid Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Protection Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Protection Active", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Enables the ZenDroid Guardian and app interventions.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = isProtectionEnabled,
                    onCheckedChange = {
                        isProtectionEnabled = it
                        repository.setProtectionEnabled(it)
                    }
                )
            }

            Divider()

            // Friction Level
            Column {
                Text("Friction Level", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Choose how much friction you want when opening restricted apps.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                FrictionEngine.FrictionLevel.entries.forEach { level ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (level == frictionLevel),
                                onClick = {
                                    frictionLevel = level
                                    repository.setFrictionLevel(level)
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (level == frictionLevel),
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = level.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Divider()

            // Accessibility Link
            Button(
                onClick = {
                    context.startActivity(android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Open Accessibility Settings")
            }
        }
    }
}
