package com.zendroid.launcher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zendroid.launcher.data.repository.SettingsRepository
import com.zendroid.launcher.ui.intervention.friction.FrictionEngine
import com.zendroid.launcher.ui.schedules.SchedulesActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumSettingsScreen(
                repository = settingsRepository,
                onBack = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSettingsScreen(
    repository: SettingsRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isProtectionEnabled by remember { mutableStateOf(repository.isProtectionEnabled()) }
    var frictionLevel by remember { mutableStateOf(repository.getFrictionLevel()) }
    var redirectPackage by remember { mutableStateOf(repository.getRedirectPackage()) }
    var zenLevel by remember { mutableStateOf(repository.getZenTitrationLevel()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Guardian
            SettingsCard(title = "Guardian Shield", icon = Icons.Default.Security) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Protection", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("Enable app interventions", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
                    }
                    Switch(
                        checked = isProtectionEnabled,
                        onCheckedChange = {
                            isProtectionEnabled = it
                            repository.setProtectionEnabled(it)
                        }
                    )
                }
            }

            // Section 2: App Management
            SettingsCard(title = "App Management", icon = Icons.Default.Info) {
                Text(
                    "Categorize your apps as Green (Safe), Yellow (Caution), or Red (Distraction).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, AppCategorizationActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                ) {
                    Text("Categorize Apps")
                }
            }

            // Section: Zen Titration (Visual Friction)
            SettingsCard(title = "Zen Titration", icon = Icons.Default.Visibility) {
                Text(
                    "Control how 'luring' your apps look. Titrating icons reduces the subconscious dopamine search.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Slider(
                    value = zenLevel.ordinal.toFloat(),
                    onValueChange = {
                        val newLevel = SettingsRepository.ZenTitrationLevel.entries[it.toInt()]
                        zenLevel = newLevel
                        repository.setZenTitrationLevel(newLevel)
                    },
                    valueRange = 0f..4f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labels = listOf("Normal", "Muted", "Mono", "Ghost", "Void")
                    labels.forEachIndexed { index, label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (zenLevel.ordinal == index) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 8.sp
                        )
                    }
                }
            }

            // Section 3: Friction Level
            SettingsCard(title = "Friction Level", icon = Icons.Default.Info) {
                Text(
                    "Balance between mindful pause and accessibility.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FrictionEngine.FrictionLevel.entries.forEach { level ->
                    val isSelected = level == frictionLevel
                    Surface(
                        onClick = {
                            frictionLevel = level
                            repository.setFrictionLevel(level)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(level.name.capitalize(), color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f))
                                val detail = when(level) {
                                    FrictionEngine.FrictionLevel.LOW -> "Quick 3s hold"
                                    FrictionEngine.FrictionLevel.MEDIUM -> "5s hold + reason"
                                    FrictionEngine.FrictionLevel.HIGH -> "10s hold + breathing"
                                    FrictionEngine.FrictionLevel.EXTREME -> "Math problems + high friction"
                                }
                                Text(detail, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
                // Section 3: Focus Profiles
            SettingsCard(title = "Focus Profiles", icon = Icons.Default.Schedule) {
                Text(
                    "Automatic protection schedules for deep work or sleep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, SchedulesActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                ) {
                    Text("Manage Schedules")
                }
            }

            // Section 4: Mindful Redirect
            SettingsCard(title = "Mindful Redirection", icon = Icons.Default.SwapHoriz) {
                Text(
                    "Choose an app to redirect to when you need a mindful alternative.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = redirectPackage,
                    onValueChange = {
                        redirectPackage = it
                        repository.setRedirectPackage(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Redirect Package Name") },
                    placeholder = { Text("e.g. com.amazon.kindle") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            // Section 5: Shield Health
            SettingsCard(title = "Shield Health", icon = Icons.Default.Security) {
                Text(
                    "Ensure ZenDroid can't be killed by the system.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Button(
                    onClick = {
                        context.startActivity(Intent(context, ShieldHealthActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Check Shield Health")
                }
            }
        }
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

private fun String.capitalize() = this.lowercase().replaceFirstChar { it.uppercase() }
