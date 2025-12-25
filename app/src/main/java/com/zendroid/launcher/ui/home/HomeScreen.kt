package com.zendroid.launcher.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.zendroid.launcher.data.repository.SettingsRepository
import com.zendroid.launcher.data.db.AppEntity
import com.zendroid.launcher.util.PerformanceMonitor
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background

@Composable
fun HomeScreen(
    onOpenHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Note: PerformanceMonitor.trace cannot wrap @Composable content directly
    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Success -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mindful Dashboard Header
                    MindfulDashboard(
                        pauseCount = viewModel.mindfulPauses.collectAsState().value,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable { onOpenHistory() }
                    )

                    AppList(
                        apps = state.apps,
                        zenLevel = viewModel.zenLevel.collectAsState().value,
                        onAppClick = { app ->
                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (intent != null) {
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            }
                        },
                        onGetIcon = { viewModel.getIcon(it) },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Search Bar (Bottom)
                    TextField(
                        value = viewModel.searchQuery.collectAsState().value, 
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = { Text("Type to search...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        singleLine = true,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.TextFieldDefaults.colors(
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MindfulDashboard(
    pauseCount: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Mindful Pauses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = pauseCount.toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (pauseCount > 0) "Stay focused!" else "Ready for a deep work day?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AppList(
    apps: List<AppEntity>,
    zenLevel: SettingsRepository.ZenTitrationLevel,
    onAppClick: (AppEntity) -> Unit,
    onGetIcon: (String) -> androidx.compose.ui.graphics.ImageBitmap?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
    ) {
        items(
            items = apps,
            key = { it.packageName }
        ) { app ->
            val indicatorColor = when (app.category) {
                "RED" -> Color(0xFFFF4B4B)
                "YELLOW" -> Color(0xFFFFB800)
                else -> Color.Transparent
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onAppClick(app) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Indicator
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor)
                )
                
                Spacer(modifier = Modifier.width(16.dp))

                // Zen Icon (Titrated)
                if (zenLevel != SettingsRepository.ZenTitrationLevel.VOID) {
                    val iconBitmap = remember(app.packageName) {
                        onGetIcon(app.packageName)
                    }

                    if (iconBitmap != null) {
                        val alpha = if (zenLevel == SettingsRepository.ZenTitrationLevel.GHOST) 0.15f else 1f
                        val colorMatrix = remember(zenLevel) {
                            ColorMatrix().apply {
                                when (zenLevel) {
                                    SettingsRepository.ZenTitrationLevel.MUTED -> setToSaturation(0.4f)
                                    SettingsRepository.ZenTitrationLevel.MONO -> setToSaturation(0f)
                                    else -> {} 
                                }
                            }
                        }

                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .alpha(alpha),
                            colorFilter = if (zenLevel == SettingsRepository.ZenTitrationLevel.MUTED || zenLevel == SettingsRepository.ZenTitrationLevel.MONO)
                                ColorFilter.colorMatrix(colorMatrix) else null
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                }
                
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
