package com.zendroid.launcher.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.zendroid.launcher.data.repository.SettingsRepository
import com.zendroid.launcher.data.db.AppEntity
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip

@AndroidEntryPoint
class AppCategorizationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppCategorizationScreen(onBack = { finish() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCategorizationScreen(
    onBack: () -> Unit,
    viewModel: AppCategorizationViewModel = hiltViewModel()
) {
    val apps by viewModel.appsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val zenLevel = viewModel.zenLevel.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Management", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                placeholder = { Text("Search apps...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    AppCategoryItem(
                        app = app,
                        zenLevel = zenLevel,
                        viewModel = viewModel,
                        onCategoryChange = { newCat -> viewModel.updateCategory(app.packageName, newCat) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppCategoryItem(
    app: AppEntity,
    zenLevel: SettingsRepository.ZenTitrationLevel,
    viewModel: AppCategorizationViewModel,
    onCategoryChange: (String) -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zen Icon (Titrated)
            if (zenLevel != SettingsRepository.ZenTitrationLevel.VOID) {
                val iconBitmap = remember(app.packageName) {
                    viewModel.getIcon(app.packageName)
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
                Text(app.label, fontWeight = FontWeight.Bold, color = Color.White)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.3f), maxLines = 1)
            }
            
            // 3-Way Selector
            Row(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.05f)),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("GREEN", "YELLOW", "RED").forEach { cat ->
                    val isSelected = app.category == cat
                    val color = when(cat) {
                        "RED" -> Color(0xFFFF5252)
                        "YELLOW" -> Color(0xFFFFD740)
                        else -> Color(0xFF4CAF50)
                    }
                    
                    Surface(
                        onClick = { onCategoryChange(cat) },
                        color = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                        modifier = Modifier.size(width = 60.dp, height = 32.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = cat.take(1),
                                color = if (isSelected) color else Color.White.copy(alpha = 0.2f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
