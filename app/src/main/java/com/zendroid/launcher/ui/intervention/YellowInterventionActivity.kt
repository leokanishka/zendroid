package com.zendroid.launcher.ui.intervention

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.zendroid.launcher.data.repository.SessionRepository
import com.zendroid.launcher.domain.InterventionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.zendroid.launcher.data.repository.SettingsRepository
import androidx.compose.foundation.BorderStroke

@AndroidEntryPoint
class YellowInterventionActivity : ComponentActivity() {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var interventionManager: InterventionManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
    }

    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
        
        setContent {
            YellowInterventionScreen(
                targetPackage = targetPackage,
                settingsRepository = settingsRepository,
                onUnlocked = {
                    grantSessionAndLaunch()
                },
                onCancel = {
                    interventionManager.resetInterventionLock()
                    finish()
                }
            )
        }
    }

    private fun grantSessionAndLaunch() {
        lifecycleScope.launch {
            // Yellow apps default to a fixed duration or we can just say 15m for now
            sessionRepository.createSession(targetPackage, 15, "Yellow App Unlock")
            interventionManager.resetInterventionLock()
            
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            }
            finish()
        }
    }

    // Back button disabled to prevent friction bypass (Critical Gap Fix 1)
}

@Composable
fun YellowInterventionScreen(
    targetPackage: String,
    settingsRepository: SettingsRepository,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }
    val appInfo = remember {
        try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(targetPackage, 0)
            val label = pm.getApplicationLabel(info).toString()
            val icon = pm.getApplicationIcon(info)
            label to icon
        } catch (e: Exception) {
            "Unknown App" to null
        }
    }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onClick = onCancel) // Tap outside to cancel
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(500)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clickable(enabled = false) {} // Prevent clicks from going through board
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E1E2E).copy(alpha = 0.95f),
                                Color(0xFF0F0F1A).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Pull bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // App Icon & Info (Titrated)
                    val zenLevel = remember { settingsRepository.getZenTitrationLevel() }
                    if (zenLevel != SettingsRepository.ZenTitrationLevel.VOID) {
                        appInfo.second?.let { icon ->
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
                                bitmap = icon.toBitmap().asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .alpha(alpha),
                                colorFilter = if (zenLevel == SettingsRepository.ZenTitrationLevel.MUTED || zenLevel == SettingsRepository.ZenTitrationLevel.MONO)
                                    ColorFilter.colorMatrix(colorMatrix) else null
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = appInfo.first,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "Take a mindful pause before proceeding.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    HoldToUnlockButton(onUnlocked = onUnlocked)
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Mindful Redirect (80/20 Value Add)
                    MindfulRedirectButton(
                        onRedirect = onCancel,
                        settingsRepository = settingsRepository
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(onClick = onCancel) {
                        Text(
                            text = "Nevermind",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoldToUnlockButton(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val vibrator = remember { 
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.os.VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.os.Vibrator::class.java)
        }
    }
    
    var progress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(100), label = "HoldProgress"
    )
    var isPressing by remember { mutableStateOf(false) }

    LaunchedEffect(isPressing) {
        if (isPressing) {
            // Haptic feedback on press start
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
            
            val startTime = System.currentTimeMillis()
            val duration = 3000f // 3 seconds
            while (isPressing && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed / duration).coerceIn(0f, 1f)
                if (progress >= 1f) {
                    // Success haptic feedback
                    vibrator?.let {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            it.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            it.vibrate(100)
                        }
                    }
                    onUnlocked()
                    break
                }
                delay(16) // ~60fps
            }
        } else {
            progress = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(120.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressing = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressing = false
                        }
                    }
                )
            }
    ) {
        // Base Circle
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.05f),
            modifier = Modifier.fillMaxSize()
        ) {}
        
        // Progress Ring
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            strokeWidth = 4.dp,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Icon
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    Text(
        text = if (isPressing) "Hold..." else "Hold to unlock",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )
}
