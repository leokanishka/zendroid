package com.zendroid.launcher.ui.intervention

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.Icon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import com.zendroid.launcher.data.repository.SessionRepository
import com.zendroid.launcher.data.repository.SettingsRepository
import com.zendroid.launcher.domain.InterventionManager
import com.zendroid.launcher.ui.intervention.friction.FrictionEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Full-screen Intervention Activity displayed when user opens a "Red" app.
 */
@AndroidEntryPoint
class InterventionActivity : ComponentActivity() {

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
            InterventionScreen(
                targetPackage = targetPackage,
                settingsRepository = settingsRepository,
                onSessionGranted = { durationMinutes, reason ->
                    grantSessionAndLaunch(durationMinutes, reason)
                },
                onDismiss = { 
                    interventionManager.resetInterventionLock()
                    finish() 
                }
            )
        }
    }

    private fun grantSessionAndLaunch(durationMinutes: Int, reason: String) {
        lifecycleScope.launch {
            sessionRepository.createSession(targetPackage, durationMinutes, reason)
            interventionManager.resetInterventionLock()
            
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launchIntent)
            }
            finish()
        }
    }

    @Deprecated("Use OnBackPressedDispatcher", ReplaceWith("onBackPressedDispatcher"))
    override fun onBackPressed() {
        // Friction: back button disabled
    }
}

@Composable
fun InterventionScreen(
    targetPackage: String,
    settingsRepository: SettingsRepository,
    onSessionGranted: (durationMinutes: Int, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStage by remember { mutableStateOf(InterventionStage.REASON) }
    var reason by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(5) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE61A1A2E)) // Semi-transparent dark overlay
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (currentStage) {
                InterventionStage.REASON -> {
                    ReasonStage(
                        settingsRepository = settingsRepository,
                        onReasonSubmitted = { submittedReason ->
                            reason = submittedReason
                            currentStage = InterventionStage.DURATION
                        },
                        onDismiss = onDismiss
                    )
                }
                InterventionStage.DURATION -> {
                    DurationStage(
                        onDurationSelected = { duration ->
                            selectedDuration = duration
                            currentStage = InterventionStage.FRICTION
                        },
                        onBack = { currentStage = InterventionStage.REASON }
                    )
                }
                InterventionStage.FRICTION -> {
                    FrictionStage(
                        frictionLevel = settingsRepository.getFrictionLevel(),
                        onFrictionCompleted = {
                            onSessionGranted(selectedDuration, reason)
                        },
                        onCancel = onDismiss
                    )
                }
            }
        }
    }
}

enum class InterventionStage {
    REASON,
    DURATION,
    FRICTION
}

@Composable
fun ReasonStage(
    settingsRepository: SettingsRepository,
    onReasonSubmitted: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val zenLevel = remember { settingsRepository.getZenTitrationLevel() }
        
        // Zen Icon (Titrated)
        if (zenLevel != SettingsRepository.ZenTitrationLevel.VOID) {
            val context = LocalContext.current
            val targetPackage = (context as? InterventionActivity)?.intent?.getStringExtra(InterventionActivity.EXTRA_TARGET_PACKAGE) ?: ""
            val iconBitmap = remember(targetPackage) {
                try {
                    val drawable = context.packageManager.getApplicationIcon(targetPackage)
                    drawable.toBitmap().asImageBitmap()
                } catch (e: Exception) {
                    null
                }
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
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .alpha(alpha),
                    colorFilter = if (zenLevel == SettingsRepository.ZenTitrationLevel.MUTED || zenLevel == SettingsRepository.ZenTitrationLevel.MONO)
                        ColorFilter.colorMatrix(colorMatrix) else null
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        Text(
            text = "Wait.",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Why do you need to open this app right now?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter your reason...", color = Color.White.copy(alpha = 0.3f)) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.White,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.5f)
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Mindful Redirect (80/20 Value Add)
        MindfulRedirectButton(
            onRedirect = onDismiss,
            settingsRepository = settingsRepository
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { if (text.isNotBlank()) onReasonSubmitted(text) },
                enabled = text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("Next")
            }
        }
    }
}

@Composable
fun DurationStage(
    onDurationSelected: (Int) -> Unit,
    onBack: () -> Unit
) {
    val options = listOf(5, 15, 30, 60)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "How long?",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Choose your session length.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        options.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                pair.forEach { minutes ->
                    Button(
                        onClick = { onDurationSelected(minutes) },
                        modifier = Modifier.width(120.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Text("${minutes}m", color = Color.White)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBack, colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.5f))) {
            Text("Back")
        }
    }
}

@Composable
fun FrictionStage(
    frictionLevel: FrictionEngine.FrictionLevel,
    onFrictionCompleted: () -> Unit,
    onCancel: () -> Unit
) {
    val frictionType = remember { FrictionEngine.selectFriction(frictionLevel) }
    val durationMs = FrictionEngine.getDurationMs(frictionType, frictionLevel)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (frictionType) {
            FrictionEngine.FrictionType.LIQUID_HOLD -> {
                LiquidHoldFriction(durationMs, onFrictionCompleted, onCancel)
            }
            FrictionEngine.FrictionType.BREATHING -> {
                BreathingFriction(durationMs, onFrictionCompleted, onCancel)
            }
            FrictionEngine.FrictionType.MATH -> {
                MathFriction(onFrictionCompleted, onCancel)
            }
        }
    }
}

@Composable
fun LiquidHoldFriction(durationMs: Long, onComplete: () -> Unit, onCancel: () -> Unit) {
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
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "HoldProgress")
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
            while (isPressing && progress < 1f) {
                val elapsed = System.currentTimeMillis() - startTime
                progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
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
                    onComplete()
                    break
                }
                delay(16)
            }
        } else {
            progress = 0f
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Wait...", style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Hold to proceed", style = MaterialTheme.typography.titleLarge, color = Color.White.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(64.dp))
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp)
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
            // Background fill
            CircularProgressIndicator(
                progress = 1f,
                modifier = Modifier.fillMaxSize(),
                color = Color.White.copy(alpha = 0.1f),
                strokeWidth = 12.dp
            )
            
            // Progress fill
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxSize(),
                color = Color.White,
                strokeWidth = 12.dp
            )

            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge
            )
        }
        
        Spacer(modifier = Modifier.height(64.dp))
        TextButton(onClick = onCancel) { 
            Text("Actually, nevermind", color = Color.White.copy(alpha = 0.5f)) 
        }
    }
}

@Composable
fun BreathingFriction(durationMs: Long, onComplete: () -> Unit, onCancel: () -> Unit) {
    var timeLeft by remember { mutableStateOf(durationMs / 1000) }
    
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        onComplete()
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Just breathe...", style = MaterialTheme.typography.displaySmall, color = Color.White)
        Spacer(modifier = Modifier.height(64.dp))
        // Placeholder for breathing animation
        CircularProgressIndicator(
            progress = (durationMs / 1000 - timeLeft).toFloat() / (durationMs / 1000),
            modifier = Modifier.size(120.dp),
            color = Color.White,
            strokeWidth = 8.dp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("${timeLeft}s", color = Color.White, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(64.dp))
        TextButton(onClick = onCancel) { Text("Exit", color = Color.White.copy(alpha = 0.5f)) }
    }
}

@Composable
fun MathFriction(onComplete: () -> Unit, onCancel: () -> Unit) {
    val problem = remember { FrictionEngine.generateMathProblem() }
    var answer by remember { mutableStateOf("") }
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quick focus check", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        Text(problem.first, style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = answer,
            onValueChange = { answer = it },
            label = { Text("Answer") },
            modifier = Modifier.width(200.dp),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { if (answer == problem.second.toString()) onComplete() },
            enabled = answer.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("Unlock")
        }
        TextButton(onClick = onCancel) { Text("I can't even", color = Color.White.copy(alpha = 0.5f)) }
    }
}
@Composable
fun MindfulRedirectButton(onRedirect: () -> Unit, settingsRepository: SettingsRepository) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val redirectPackage = remember { settingsRepository.getRedirectPackage() }
    
    OutlinedButton(
        onClick = { 
            val intent = context.packageManager.getLaunchIntentForPackage(redirectPackage) ?: 
                         Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_HOME) }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            onRedirect()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Book, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Read a book instead", color = Color.White)
        }
    }
}
