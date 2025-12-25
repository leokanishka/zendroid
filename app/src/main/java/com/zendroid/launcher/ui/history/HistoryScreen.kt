package com.zendroid.launcher.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zendroid.launcher.data.db.SessionEntity
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.zendroid.launcher.data.repository.SettingsRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.historyState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mindful Insights", fontWeight = FontWeight.Bold) },
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
        containerColor = Color(0xFF0F0F1A) // Deep dark background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is HistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                }
                is HistoryUiState.Empty -> {
                    EmptyHistory(modifier = Modifier.align(Alignment.Center))
                }
                is HistoryUiState.Success -> {
                    HistoryContent(
                        sessions = state.sessions, 
                        analytics = state.analytics, 
                        zenLevel = viewModel.zenLevel.collectAsState().value,
                        onGetIcon = { viewModel.getIcon(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryContent(
    sessions: List<SessionEntity>,
    analytics: HistoryAnalytics,
    zenLevel: SettingsRepository.ZenTitrationLevel,
    onGetIcon: (String) -> ImageBitmap?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Analytics Summary Card
        item {
            AnalyticsSummaryCard(analytics)
        }

        item {
            Text(
                text = "Recent Pauses",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(sessions) { session ->
            HistoryItem(session, zenLevel, onGetIcon)
        }
    }
}

@Composable
fun AnalyticsSummaryCard(analytics: HistoryAnalytics) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Total Focus Today", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
            }
            Text(
                text = "${analytics.totalMindfulMinutes}m",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Top Distraction Triggers", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(12.dp))
            
            analytics.topReasons.forEach { (reason, count) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(reason.ifBlank { "Unspecified" }, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Text("${count}", color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun HistoryItem(
    session: SessionEntity, 
    zenLevel: SettingsRepository.ZenTitrationLevel,
    onGetIcon: (String) -> ImageBitmap?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.03f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zen Icon (Titrated)
            if (zenLevel != SettingsRepository.ZenTitrationLevel.VOID) {
                val iconBitmap = remember(session.packageName) {
                    onGetIcon(session.packageName)
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

            Column(modifier = Modifier.weight(1f)) {
                val context = LocalContext.current
                val appLabel = remember(session.packageName) {
                    try {
                        val pm = context.packageManager
                        val info = pm.getApplicationInfo(session.packageName, 0)
                        pm.getApplicationLabel(info).toString()
                    } catch (e: Exception) {
                        session.packageName.split(".").last().capitalize()
                    }
                }
                Text(
                    text = appLabel,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = session.reason.ifBlank { "Mindful pause" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${session.durationMinutes}m",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun EmptyHistory(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.White.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No mindful pauses yet", color = Color.White.copy(alpha = 0.4f))
    }
}
